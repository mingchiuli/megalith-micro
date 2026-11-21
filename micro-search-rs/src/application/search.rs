use super::Search;
use super::contract::{AdminQuery, RevisionQuery, SearchPage};
use crate::domain::IndexKind;
use crate::domain::{self, HIGHLIGHT};
use anyhow::{Context, Result};
use serde_json::{Value, json};

impl Search {
    async fn query(&self, kind: IndexKind, query: Value) -> Result<(Vec<Value>, u64)> {
        let result = self
            .engine
            .post(
                &format!("/indexes/{}/search", self.cfg.index_uid(kind)),
                query,
            )
            .await?;
        Ok((
            domain::rows(&result["hits"])?.to_vec(),
            result["totalHits"].as_u64().context("missing totalHits")?,
        ))
    }
    pub async fn public_search(&self, page: u32, keywords: String, all: bool) -> Result<Value> {
        let size = self.cfg.page_size;
        let fields = ["title", "description", "content"];
        let mut q = json!({
            "q": keywords, "page": page, "hitsPerPage": size,
            "filter": "deleted = false AND status = 0",
            "attributesToHighlight": fields, "highlightPreTag": HIGHLIGHT,
            "highlightPostTag": "</b>", "showRankingScore": true
        });
        if !all {
            q["attributesToCrop"] = json!(fields);
            q["cropLength"] = 12.into();
        }
        let (hits, total) = self.query(IndexKind::Public, q).await?;
        let pages = total.div_ceil(size as u64);
        let content = hits.iter().map(domain::hit).collect::<Result<Vec<_>>>()?;
        Ok(json!({
            "content": content,
            "totalElements": total, "pageNumber": page, "pageSize": size,
            "first": page == 1, "last": page as u64 >= pages,
            "empty": total == 0, "totalPages": pages
        }))
    }
    pub async fn admin_search(&self, q: AdminQuery) -> Result<SearchPage> {
        let draft = q.draft_only == Some(true) || q.status == Some(3);
        let mut filters = vec!["deleted = false".into()];
        filters.push(
            if draft {
                "hasDraft = true"
            } else {
                "publishedRevisionId IS NOT NULL AND publishedRevisionId EXISTS"
            }
            .into(),
        );
        if !draft && let Some(status) = q.status {
            filters.push(format!("status = {status}"));
        }
        if !q.all_data {
            filters.push(format!("ownerKey = u{}", q.user_id));
        }
        if let Some(date) = q.create_start {
            filters.push(format!("createdMillis >= {}", domain::millis(&date)?));
        }
        if let Some(date) = q.create_end {
            filters.push(format!("createdMillis <= {}", domain::millis(&date)?));
        }
        let fields = if draft {
            ["draftTitle", "draftDescription", "draftContent"]
        } else {
            ["publishedTitle", "publishedDescription", "publishedContent"]
        };
        let params = json!({
            "q": q.keywords.unwrap_or_default(), "page": q.page, "hitsPerPage": q.page_size,
            "filter": filters, "attributesToSearchOn": fields, "sort": ["createdMillis:desc", "id:asc"]
        });
        let (hits, total) = self.query(IndexKind::Admin, params).await?;
        let content = hits
            .iter()
            .map(|doc| domain::admin_read(doc, draft))
            .collect::<Vec<_>>();
        Ok(SearchPage {
            total,
            current_page: q.page,
            size: q.page_size,
            content,
        })
    }
    pub async fn revisions(&self, q: RevisionQuery) -> Result<SearchPage> {
        let article = self
            .engine
            .doc(self.cfg.index_uid(IndexKind::Admin), q.blog_id)
            .await?;
        if article.is_null()
            || article["deleted"] == true
            || (q.all_data != Some(true) && article["userId"].as_i64() != Some(q.user_id))
        {
            return Ok(SearchPage {
                total: 0,
                current_page: q.page,
                size: q.page_size,
                content: Vec::new(),
            });
        }
        let published = article["publishedRevisionId"].as_i64();
        let generation = domain::history_generation(&article)?;
        let filters = [
            format!("articleKey = b{}", q.blog_id),
            format!("historyKey = g{generation}"),
        ];
        let params = json!({
            "q": q.keywords.unwrap_or_default(), "page": q.page, "hitsPerPage": q.page_size,
            "filter": filters, "sort": ["versionNo:desc"]
        });
        let (hits, total) = self.query(IndexKind::Revision, params).await?;
        let content = hits
            .iter()
            .map(|h| domain::revision_read(h, published))
            .collect::<Result<Vec<_>>>()?;
        Ok(SearchPage {
            total,
            current_page: q.page,
            size: q.page_size,
            content,
        })
    }
}
