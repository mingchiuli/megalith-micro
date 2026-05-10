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
    // Remove javascript: URLs
    .replace(/javascript:/gi, '')
    // Remove data: URLs that could contain malicious content
    .replace(/data:\s*text\/html/gi, '')

  return sanitized
}

/**
 * Sanitize markdown rendered content
 * Removes script tags and other dangerous elements
 */
export const sanitizeMarkdown = (html: string): string => {
  if (!html) return ''

  // Remove script tags
  const sanitized = html
    .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '')
    // Remove iframe tags
    .replace(/<iframe\b[^>]*>.*?<\/iframe>/gi, '')
    // Remove object/embed tags
    .replace(/<object\b[^>]*>.*?<\/object>/gi, '')
    .replace(/<embed\b[^>]*>/gi, '')
    // Remove event handlers
    .replace(/on\w+="[^"]*"/gi, '')
    .replace(/on\w+='[^']*'/gi, '')

  return sanitized
}