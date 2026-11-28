import { expect, type Page } from '@playwright/test'

export async function prepareArticles(page: Page, prefix: string) {
  const origin = new URL(page.url()).origin
  async function post<T>(path: string, body: unknown): Promise<T> {
    const response = await page.request.post(origin + '/api' + path, {
      headers: { Origin: origin },
      data: body
    })
    expect(response.status()).toBe(200)
    return ((await response.json()) as { data: T }).data
  }
  const ids: number[] = []
  for (let index = 1; index <= 7; index++) {
    const { id, collaborationEpoch } = await post<{ id: number; collaborationEpoch: string }>(
      '/sys/blog/draft/new',
      {}
    )
    const data = {
      id,
      collaborationEpoch,
      title: prefix + ' ' + index,
      description: 'List fixture ' + index,
      content:
        '# Overview\n\n' + 'Fixture paragraph.\n\n'.repeat(24) + '# Closing\n\nFinal paragraph.',
      status: 0,
      link: '',
      sensitiveContentList: []
    }
    const saved = await post<{ draftRevision: number }>(`/sys/blog/${id}/draft`, data)
    const revision = await post<{ version: { id: number } }>(`/sys/blog/${id}/versions`, {
      ...data,
      draftRevision: saved.draftRevision
    })
    await post(`/sys/blog/${id}/publish`, { revisionId: revision.version.id })
    ids.push(id)
  }
  return ids
}
