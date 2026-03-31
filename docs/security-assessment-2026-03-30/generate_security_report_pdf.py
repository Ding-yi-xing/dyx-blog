from pathlib import Path
import re
from xml.sax.saxutils import escape
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.enums import TA_CENTER
from reportlab.lib.units import mm
from reportlab.lib import colors
from reportlab.platypus import BaseDocTemplate, PageTemplate, Frame, Paragraph, Spacer, PageBreak, Preformatted
from reportlab.platypus.tableofcontents import TableOfContents
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont

BASE = Path(r"C:\Users\40179\Documents\AllProjecks\dyx-blog\docs\security-assessment-2026-03-30")
FONT_PATH = Path(r"C:\Windows\Fonts\msyh.ttc")

pdfmetrics.registerFont(TTFont("MSYH", str(FONT_PATH)))

styles = getSampleStyleSheet()
styles.add(ParagraphStyle(name='CNTitle', parent=styles['Title'], fontName='MSYH', fontSize=24, leading=32, alignment=TA_CENTER, textColor=colors.HexColor('#7f1d1d')))
styles.add(ParagraphStyle(name='CNSubtitle', parent=styles['Normal'], fontName='MSYH', fontSize=12, leading=18, alignment=TA_CENTER, textColor=colors.HexColor('#4b5563')))
styles.add(ParagraphStyle(name='CNH1', parent=styles['Heading1'], fontName='MSYH', fontSize=18, leading=24, spaceBefore=16, spaceAfter=8, textColor=colors.HexColor('#7f1d1d')))
styles.add(ParagraphStyle(name='CNH2', parent=styles['Heading2'], fontName='MSYH', fontSize=15, leading=22, spaceBefore=12, spaceAfter=6, textColor=colors.HexColor('#991b1b')))
styles.add(ParagraphStyle(name='CNH3', parent=styles['Heading3'], fontName='MSYH', fontSize=13, leading=19, spaceBefore=10, spaceAfter=4, textColor=colors.HexColor('#b91c1c')))
styles.add(ParagraphStyle(name='CNBody', parent=styles['BodyText'], fontName='MSYH', fontSize=10.5, leading=17, spaceAfter=6))
styles.add(ParagraphStyle(name='CNBullet', parent=styles['BodyText'], fontName='MSYH', fontSize=10.5, leading=17, leftIndent=14, firstLineIndent=-10, spaceAfter=3))
styles.add(ParagraphStyle(name='CNCode', parent=styles['Code'], fontName='Courier', fontSize=8.2, leading=10.5, leftIndent=8, rightIndent=8, backColor=colors.HexColor('#f8fafc'), borderPadding=6, borderColor=colors.HexColor('#e5e7eb'), borderWidth=0.3, borderRadius=2, spaceAfter=8))

class MyDocTemplate(BaseDocTemplate):
    def __init__(self, filename, **kwargs):
        super().__init__(filename, **kwargs)
        frame = Frame(self.leftMargin, self.bottomMargin, self.width, self.height, id='normal')
        template = PageTemplate(id='main', frames=[frame], onPage=self.draw_page)
        self.addPageTemplates([template])

    def draw_page(self, canvas, doc):
        canvas.saveState()
        canvas.setStrokeColor(colors.HexColor('#e5e7eb'))
        canvas.line(doc.leftMargin, 18 * mm, A4[0] - doc.rightMargin, 18 * mm)
        canvas.setFont('MSYH', 9)
        canvas.setFillColor(colors.HexColor('#6b7280'))
        canvas.drawString(doc.leftMargin, 11 * mm, 'dyx-blog 安全检测报告')
        canvas.drawRightString(A4[0] - doc.rightMargin, 11 * mm, str(canvas.getPageNumber()))
        canvas.restoreState()

    def afterFlowable(self, flowable):
        if hasattr(flowable, '_bookmarkName') and hasattr(flowable, '_level'):
            self.canv.bookmarkPage(flowable._bookmarkName)
            self.notify('TOCEntry', (flowable._level, flowable.getPlainText(), self.page, flowable._bookmarkName))


def heading_para(text, level, idx):
    style = {1: styles['CNH1'], 2: styles['CNH2'], 3: styles['CNH3']}.get(level, styles['CNH3'])
    p = Paragraph(escape(text), style)
    p._bookmarkName = f'h{level}_{idx}'
    p._level = min(level, 3)
    return p


def preprocess_inline(text):
    text = escape(text)
    text = re.sub(r'`([^`]+)`', lambda m: f'<font name="Courier">{escape(m.group(1))}</font>', text)
    return text.replace('\n', '<br/>')


def md_to_story(md_path: Path):
    lines = md_path.read_text(encoding='utf-8').splitlines()
    story = []
    story.append(Spacer(1, 45 * mm))
    story.append(Paragraph('dyx-blog 安全检测报告', styles['CNTitle']))
    story.append(Spacer(1, 8 * mm))
    story.append(Paragraph('仓库审计 + 低影响在线验证 + 风险分级', styles['CNSubtitle']))
    story.append(Spacer(1, 10 * mm))
    story.append(Paragraph('生成日期：2026-03-30', styles['CNSubtitle']))
    story.append(Spacer(1, 55 * mm))
    story.append(Paragraph('交付目录：docs/security-assessment-2026-03-30', styles['CNSubtitle']))
    story.append(PageBreak())

    toc_title = Paragraph('目录', styles['CNH1'])
    toc_title._bookmarkName = 'toc'
    toc_title._level = 1
    story.append(toc_title)
    toc = TableOfContents()
    toc.levelStyles = [
        ParagraphStyle(fontName='MSYH', name='TOCLevel1', fontSize=11, leading=18, leftIndent=8, firstLineIndent=-8),
        ParagraphStyle(fontName='MSYH', name='TOCLevel2', fontSize=10, leading=16, leftIndent=22, firstLineIndent=-8),
        ParagraphStyle(fontName='MSYH', name='TOCLevel3', fontSize=9.5, leading=15, leftIndent=36, firstLineIndent=-8),
    ]
    story.append(toc)
    story.append(PageBreak())

    in_code = False
    code_buf = []
    heading_idx = 0

    def flush_code():
        nonlocal code_buf
        if code_buf:
            story.append(Preformatted('\n'.join(code_buf), styles['CNCode']))
            code_buf = []

    i = 0
    while i < len(lines):
        line = lines[i]
        stripped = line.strip()
        if stripped.startswith('```'):
            if in_code:
                flush_code()
                in_code = False
            else:
                in_code = True
            i += 1
            continue
        if in_code:
            code_buf.append(line)
            i += 1
            continue
        if not stripped:
            story.append(Spacer(1, 2 * mm))
            i += 1
            continue
        if stripped.startswith('#'):
            flush_code()
            level = len(stripped) - len(stripped.lstrip('#'))
            text = stripped[level:].strip()
            if level <= 3:
                heading_idx += 1
                story.append(heading_para(text, level, heading_idx))
            else:
                story.append(Paragraph(preprocess_inline(text), styles['CNBody']))
            i += 1
            continue
        if stripped.startswith('|'):
            flush_code()
            table_lines = [line]
            i += 1
            while i < len(lines) and lines[i].strip().startswith('|'):
                table_lines.append(lines[i])
                i += 1
            story.append(Preformatted('\n'.join(table_lines), styles['CNCode']))
            continue
        if re.match(r'^[-*]\s+', stripped) or re.match(r'^\d+\.\s+', stripped):
            flush_code()
            bullet_text = re.sub(r'^([-*]|\d+\.)\s+', '• ', stripped)
            story.append(Paragraph(preprocess_inline(bullet_text), styles['CNBullet']))
            i += 1
            continue
        flush_code()
        para_lines = [stripped]
        i += 1
        while i < len(lines):
            nxt = lines[i].strip()
            if (not nxt or nxt.startswith('#') or nxt.startswith('|') or nxt.startswith('```') or
                re.match(r'^[-*]\s+', nxt) or re.match(r'^\d+\.\s+', nxt)):
                break
            para_lines.append(nxt)
            i += 1
        story.append(Paragraph(preprocess_inline(' '.join(para_lines)), styles['CNBody']))
    flush_code()
    return story


def main():
    doc = MyDocTemplate(str(BASE / 'security-report.pdf'), pagesize=A4, leftMargin=20 * mm, rightMargin=20 * mm, topMargin=20 * mm, bottomMargin=20 * mm)
    story = md_to_story(BASE / 'security-report.md')
    doc.multiBuild(story)
    print('generated: security-report.pdf')


if __name__ == '__main__':
    main()
