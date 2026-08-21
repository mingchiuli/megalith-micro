import sanitize from 'sanitize-html'

const sanitizeConfig: sanitize.IOptions = {
  allowedTags: [...sanitize.defaults.allowedTags, 'details', 'img', 'summary'],
  allowedAttributes: {
    '*': ['aria-*', 'class', 'data-line', 'id', 'role', 'title'],
    a: ['href', 'name', 'rel', 'target'],
    col: ['span'],
    colgroup: ['span'],
    img: ['alt', 'height', 'loading', 'src', 'srcset', 'title', 'width'],
    li: ['value'],
    ol: ['start', 'type'],
    td: ['colspan', 'rowspan'],
    th: ['colspan', 'rowspan', 'scope'],
    time: ['datetime']
  },
  allowedSchemes: ['http', 'https', 'mailto', 'tel'],
  allowProtocolRelative: false,
  disallowedTagsMode: 'discard',
  nestingLimit: 100,
  parseStyleAttributes: false
}

const highlightConfig: sanitize.IOptions = {
  allowedTags: ['b', 'em', 'i', 'mark', 'strong'],
  allowedAttributes: {},
  disallowedTagsMode: 'discard',
  parseStyleAttributes: false
}

export const sanitizeHtml = (html: string): string => sanitize(html, sanitizeConfig)

export const sanitizeHighlight = (html: string): string => sanitize(html, highlightConfig)
