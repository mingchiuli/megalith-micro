import DOMPurify, { type Config } from 'dompurify'

const sanitizeConfig: Config = {
  USE_PROFILES: { html: true },
  FORBID_TAGS: ['style', 'iframe', 'object', 'embed'],
  FORBID_ATTR: ['style'],
  ADD_ATTR: ['target', 'rel', 'loading', 'data-line'],
  ALLOW_DATA_ATTR: false
}

export const sanitizeHtml = (html: string): string => DOMPurify.sanitize(html, sanitizeConfig)

export const sanitizeHighlight = (html: string): string => sanitizeHtml(html)
