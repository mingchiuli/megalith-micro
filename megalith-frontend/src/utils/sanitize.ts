/**
 * Sanitize HTML from backend to prevent XSS attacks
 * Backend search highlights use <em> tags - only allow these
 */

/**
 * Sanitize highlight HTML content
 * Removes all HTML tags except <em> for search highlighting
 */
export const sanitizeHighlight = (html: string): string => {
  if (!html) return ''

  let sanitized = html
  let previous: string

  // Repeatedly apply removals to avoid incomplete multi-character sanitization
  // where one replacement can expose another dangerous pattern.
  do {
    previous = sanitized
    sanitized = sanitized
      // Remove all HTML tags except <em> and </em>
      // Handle unclosed tags and tags with > in attribute values
      .replace(/<(?!\/?em\b)[^>]*(?:>|$)/gi, '')
      // Remove event handlers (quoted, single-quoted, and unquoted values)
      .replace(/on\w+\s*=\s*(?:"[^"]*"|'[^']*'|[^\s>]*)/gi, '')
      // Remove dangerous executable URL schemes (handle whitespace variations)
      .replace(/\b(?:javascript|data|vbscript)\s*:/gi, '')
  } while (sanitized !== previous)

  return sanitized
}

/**
 * Sanitize markdown rendered content
 * Removes script tags and other dangerous elements
 */
export const sanitizeMarkdown = (html: string): string => {
  if (!html) return ''

  let sanitized = html
  let previous: string

  // Repeatedly apply removals to avoid incomplete multi-character sanitization
  // where one replacement can expose another dangerous pattern.
  do {
    previous = sanitized
    sanitized = sanitized
      // Remove script tags (handle whitespace in end tag: </script >)
      .replace(/<script\b[^<]*(?:(?!<\/script\s*>)[^<])*<\/script\s*>/gi, '')
      // Remove unclosed script tags
      .replace(/<script\b[^>]*(?:>|$)/gi, '')
      // Remove iframe tags
      .replace(/<iframe\b[^>]*>.*?<\/iframe\s*>/gi, '')
      // Remove unclosed iframe tags
      .replace(/<iframe\b[^>]*(?:>|$)/gi, '')
      // Remove object/embed tags
      .replace(/<object\b[^>]*>.*?<\/object\s*>/gi, '')
      .replace(/<object\b[^>]*(?:>|$)/gi, '')
      .replace(/<embed\b[^>]*(?:>|$)/gi, '')
      // Remove event handlers (quoted, single-quoted, and unquoted values)
      .replace(/on\w+\s*=\s*(?:"[^"]*"|'[^']*'|[^\s>]*)/gi, '')
  } while (sanitized !== previous)

  return sanitized
}