import hljs from 'highlight.js/lib/core'
import bash from 'highlight.js/lib/languages/bash'
import css from 'highlight.js/lib/languages/css'
import java from 'highlight.js/lib/languages/java'
import javascript from 'highlight.js/lib/languages/javascript'
import json from 'highlight.js/lib/languages/json'
import kotlin from 'highlight.js/lib/languages/kotlin'
import markdown from 'highlight.js/lib/languages/markdown'
import python from 'highlight.js/lib/languages/python'
import rust from 'highlight.js/lib/languages/rust'
import sql from 'highlight.js/lib/languages/sql'
import typescript from 'highlight.js/lib/languages/typescript'
import xml from 'highlight.js/lib/languages/xml'
import yaml from 'highlight.js/lib/languages/yaml'
import MarkdownIt from 'markdown-it'
import { sanitizeHtml } from '@/utils/sanitize'

const languages = {
  bash,
  css,
  java,
  javascript,
  json,
  kotlin,
  markdown,
  python,
  rust,
  sql,
  typescript,
  xml,
  yaml
}

Object.entries(languages).forEach(([name, language]) => hljs.registerLanguage(name, language))
hljs.registerAliases(['js', 'jsx'], { languageName: 'javascript' })
hljs.registerAliases(['ts', 'tsx'], { languageName: 'typescript' })
hljs.registerAliases(['html', 'vue'], { languageName: 'xml' })
hljs.registerAliases(['sh', 'shell'], { languageName: 'bash' })
hljs.registerAliases(['md'], { languageName: 'markdown' })
hljs.registerAliases(['yml'], { languageName: 'yaml' })

const md = new MarkdownIt({
  highlight: (code: string, language: string) => {
    if (!language || !hljs.getLanguage(language)) {
      return ''
    }
    return hljs.highlight(code, { language, ignoreIllegals: true }).value
  }
})

export const render = (content: string): string => sanitizeHtml(md.render(content))
