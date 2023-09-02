import hljs from 'highlight.js';

export const markdown = (mavonEditor: any, content: string): string => {
  const md = mavonEditor.getMarkdownIt();
  return md.set(
    {
      highlight: (str: string, lang: string) => {
        const codeIndex = Date.now() + Math.floor(Math.random() * 10000000);
        let html = `<button style="background-color: white" class="copy-btn" type="button" data-clipboard-action="copy" data-clipboard-target="#copy${codeIndex}">copy</button>`;

        const linesLength = str.split(/\n/).length - 1;
        let linesNum = '<span aria-hidden="true" class="line-numbers-rows">';
        for (let index = 0; index < linesLength; index++) {
          linesNum = linesNum + "<span></span>";
        }
        linesNum += "</span>";
        if (lang && hljs.getLanguage(lang)) {
          const preCode = hljs.highlight(lang, str, true).value;
          html = html + preCode;
          if (linesLength) {
            html += '<b class="name">' + lang + "</b>";
          }
          return `<pre class="hljs"><code>${html}</code>${linesNum}</pre><textarea style="position: absolute;top: -9999px;left: -9999px;z-index: -9999;" id="copy${codeIndex}">${str.replace(
            /<\/textarea>/g,
            "</textarea>"
          )}</textarea>`
        }

        const preCode = md.utils.escapeHtml(str);
        html = html + preCode;
        return `<pre class="hljs"><code>${html}</code>${linesNum}</pre><textarea style="position: absolute;top: -9999px;left: -9999px;z-index: -9999;" id="copy${codeIndex}">${str.replace(
          /<\/textarea>/g,
          "</textarea>"
        )}</textarea>`;
      },
    }
  ).render(content);
};

