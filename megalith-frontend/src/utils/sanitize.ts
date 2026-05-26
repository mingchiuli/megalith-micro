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

  // Only allow <em> and </em> tags for highlighting
  // Remove any other HTML tags
  const sanitized = html
    // Remove all HTML tags except <em> and </em>
    .replace(/<(?!\/?em\b)[^>]+>/gi, '')
    // Remove event handlers like onclick, onerror, etc.
    .replace(/on\w+="[^"]*"/gi, '')
    .replace(/on\w+='[^']*'/gi, '')
    // Remove dangerous executable URL schemes
    .replace(/\b(?:javascript|data|vbscript)\s*:/gi, '')

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
      // Remove iframe tags
      .replace(/<iframe\b[^>]*>.*?<\/iframe\s*>/gi, '')
      // Remove object/embed tags
      .replace(/<object\b[^>]*>.*?<\/object\s*>/gi, '')
      .replace(/<embed\b[^>]*>/gi, '')
      // Remove event handlers
      .replace(/on\w+="[^"]*"/gi, '')
      .replace(/on\w+='[^']*'/gi, '')
  } while (sanitized !== previous)

  return sanitized
}