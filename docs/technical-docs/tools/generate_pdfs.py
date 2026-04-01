from __future__ import annotations

import re
from pathlib import Path
from typing import List, Tuple

from reportlab.graphics.shapes import Drawing, Line, Rect, String
from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import mm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (
    HRFlowable,
    KeepTogether,
    PageBreak,
    Paragraph,
    Preformatted,
    SimpleDocTemplate,
    Spacer,
    Table,
    TableStyle,
)

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'source'
OUTPUT = ROOT / 'output'

ZH_FONT_NAME = 'ZHFont'
MONO_FONT_NAME = 'MonoFont'
SECTION_COLORS = {
    '场景': ('#eff6ff', '#bfdbfe', '#1d4ed8'),
    '需求': ('#ecfdf5', '#a7f3d0', '#047857'),
    '实现方案': ('#fff7ed', '#fdba74', '#c2410c'),
    '选型理由': ('#f5f3ff', '#c4b5fd', '#6d28d9'),
    '核心源码定位': ('#f8fafc', '#cbd5e1', '#334155'),
    '替代方案对比': ('#fef2f2', '#fecaca', '#b91c1c'),
}


def register_fonts() -> Tuple[str, str]:
    zh_candidates = [
        Path('C:/Windows/Fonts/msyh.ttc'),
        Path('C:/Windows/Fonts/simhei.ttf'),
        Path('C:/Windows/Fonts/simsun.ttc'),
    ]
    mono_candidates = [Path('C:/Windows/Fonts/consola.ttf')]

    zh_font = next((p for p in zh_candidates if p.exists()), None)
    mono_font = next((p for p in mono_candidates if p.exists()), None)

    if not zh_font:
        raise RuntimeError('No Chinese font found for PDF generation.')

    pdfmetrics.registerFont(TTFont(ZH_FONT_NAME, str(zh_font)))
    pdfmetrics.registerFont(TTFont(MONO_FONT_NAME, str(mono_font or zh_font)))
    return str(zh_font), str(mono_font or zh_font)


def slugify(text: str) -> str:
    text = text.strip().lower()
    text = re.sub(r'\s+', '-', text)
    text = re.sub(r'[^\w\-\u4e00-\u9fff]+', '', text)
    return text or 'section'


def escape(text: str) -> str:
    return text.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')


def inline_code(text: str) -> str:
    return re.sub(
        r'`([^`]+)`',
        r'<font name="%s" textColor="#0f172a">\1</font>' % MONO_FONT_NAME,
        escape(text),
    )


def build_styles():
    base = getSampleStyleSheet()
    return {
        'cover_kicker': ParagraphStyle(
            'CoverKicker', parent=base['Normal'], fontName=ZH_FONT_NAME,
            fontSize=11, leading=14, alignment=TA_CENTER,
            textColor=colors.HexColor('#2563eb'), spaceAfter=8,
        ),
        'cover_title': ParagraphStyle(
            'CoverTitle', parent=base['Title'], fontName=ZH_FONT_NAME,
            fontSize=24, leading=30, alignment=TA_CENTER,
            textColor=colors.HexColor('#0f172a'), spaceAfter=10,
        ),
        'cover_meta': ParagraphStyle(
            'CoverMeta', parent=base['Normal'], fontName=ZH_FONT_NAME,
            fontSize=10.5, leading=15, alignment=TA_CENTER,
            textColor=colors.HexColor('#475569'), spaceAfter=4,
        ),
        'cover_note': ParagraphStyle(
            'CoverNote', parent=base['Normal'], fontName=ZH_FONT_NAME,
            fontSize=10, leading=14, alignment=TA_CENTER,
            textColor=colors.HexColor('#334155'), spaceAfter=6,
        ),
        'h1': ParagraphStyle(
            'H1', parent=base['Heading1'], fontName=ZH_FONT_NAME,
            fontSize=18, leading=24, spaceBefore=10, spaceAfter=6,
            textColor=colors.HexColor('#0f172a'),
        ),
        'h2': ParagraphStyle(
            'H2', parent=base['Heading2'], fontName=ZH_FONT_NAME,
            fontSize=14.5, leading=20, spaceBefore=8, spaceAfter=4,
            textColor=colors.HexColor('#1e293b'),
        ),
        'h3': ParagraphStyle(
            'H3', parent=base['Heading3'], fontName=ZH_FONT_NAME,
            fontSize=12, leading=16, spaceBefore=6, spaceAfter=4,
            textColor=colors.HexColor('#334155'),
        ),
        'body': ParagraphStyle(
            'Body', parent=base['BodyText'], fontName=ZH_FONT_NAME,
            fontSize=10.5, leading=12.4, spaceAfter=5,
            textColor=colors.HexColor('#111827'),
        ),
        'list': ParagraphStyle(
            'List', parent=base['BodyText'], fontName=ZH_FONT_NAME,
            fontSize=10.5, leading=12.4, leftIndent=12, firstLineIndent=-10,
            bulletIndent=0, spaceAfter=4, textColor=colors.HexColor('#111827'),
        ),
        'toc1': ParagraphStyle(
            'Toc1', parent=base['BodyText'], fontName=ZH_FONT_NAME,
            fontSize=10.5, leading=13.5, leftIndent=6,
            textColor=colors.HexColor('#1d4ed8'), spaceAfter=3,
        ),
        'toc2': ParagraphStyle(
            'Toc2', parent=base['BodyText'], fontName=ZH_FONT_NAME,
            fontSize=10, leading=12.5, leftIndent=20,
            textColor=colors.HexColor('#475569'), spaceAfter=2,
        ),
        'code_label': ParagraphStyle(
            'CodeLabel', parent=base['Normal'], fontName=MONO_FONT_NAME,
            fontSize=8.8, leading=10.8, textColor=colors.white,
        ),
        'code': ParagraphStyle(
            'Code', parent=base['Code'], fontName=MONO_FONT_NAME,
            fontSize=9.2, leading=11.6, backColor=colors.HexColor('#f8fafc'),
            borderPadding=7, borderColor=colors.HexColor('#cbd5e1'),
            borderWidth=0.6, borderRadius=4,
        ),
        'caption': ParagraphStyle(
            'Caption', parent=base['Italic'], fontName=ZH_FONT_NAME,
            fontSize=9.8, leading=12, textColor=colors.HexColor('#475569'),
            spaceBefore=4, spaceAfter=8,
        ),
        'small': ParagraphStyle(
            'Small', parent=base['Normal'], fontName=ZH_FONT_NAME,
            fontSize=9, leading=12, textColor=colors.HexColor('#64748b'),
        ),
        'table_cell': ParagraphStyle(
            'TableCell', parent=base['BodyText'], fontName=ZH_FONT_NAME,
            fontSize=9.3, leading=11.2, textColor=colors.HexColor('#111827'),
        ),
        'table_head': ParagraphStyle(
            'TableHead', parent=base['BodyText'], fontName=ZH_FONT_NAME,
            fontSize=9.3, leading=11.2, textColor=colors.HexColor('#0f172a'),
        ),
    }


def parse_markdown(md: str):
    lines = md.splitlines()
    blocks: List[Tuple[str, str, str]] = []
    in_code = False
    code_lines: List[str] = []
    code_lang = ''
    para: List[str] = []

    def flush_para():
        nonlocal para
        if para:
            blocks.append(('p', '\n'.join(para).strip(), ''))
            para = []

    for line in lines:
        if line.startswith('```'):
            if in_code:
                blocks.append(('code', '\n'.join(code_lines).rstrip(), code_lang))
                code_lines = []
                code_lang = ''
                in_code = False
            else:
                flush_para()
                in_code = True
                code_lang = line[3:].strip()
            continue
        if in_code:
            code_lines.append(line)
            continue
        if line.startswith('# '):
            flush_para()
            blocks.append(('h1_title', line[2:].strip(), ''))
        elif line.startswith('## '):
            flush_para()
            blocks.append(('h1', line[3:].strip(), ''))
        elif line.startswith('### '):
            flush_para()
            blocks.append(('h2', line[4:].strip(), ''))
        elif line.startswith('#### '):
            flush_para()
            blocks.append(('h3', line[5:].strip(), ''))
        elif re.match(r'^-\s+', line):
            flush_para()
            blocks.append(('ul', re.sub(r'^-\s+', '', line).strip(), ''))
        elif re.match(r'^\d+\.\s+', line):
            flush_para()
            blocks.append(('ol', re.sub(r'^\d+\.\s+', '', line).strip(), ''))
        elif re.match(r'^\|.+\|$', line):
            flush_para()
            blocks.append(('table', line, ''))
        elif not line.strip():
            flush_para()
        else:
            para.append(line)
    flush_para()
    return blocks


def add_box(drawing: Drawing, x: float, y: float, w: float, h: float, title: str, subtitle: str = ''):
    drawing.add(Rect(x, y, w, h, rx=8, ry=8, strokeColor=colors.HexColor('#94a3b8'), fillColor=colors.HexColor('#f8fafc')))
    drawing.add(String(x + 10, y + h - 18, title, fontName=ZH_FONT_NAME, fontSize=12, fillColor=colors.HexColor('#0f172a')))
    if subtitle:
        drawing.add(String(x + 10, y + h - 34, subtitle, fontName=ZH_FONT_NAME, fontSize=9, fillColor=colors.HexColor('#475569')))


def add_arrow(drawing: Drawing, x1: float, y1: float, x2: float, y2: float):
    drawing.add(Line(x1, y1, x2, y2, strokeColor=colors.HexColor('#64748b'), strokeWidth=1.2))


def build_frontend_diagram():
    d = Drawing(520, 280)
    add_box(d, 20, 190, 100, 48, '浏览器', 'Browser')
    add_box(d, 150, 190, 110, 48, 'Vue Router', '路由分区 / 懒加载')
    add_box(d, 290, 220, 100, 48, '前台视图', 'web/*')
    add_box(d, 290, 150, 100, 48, '后台视图', 'admin/*')
    add_box(d, 420, 220, 80, 48, 'site API', 'publicHttp')
    add_box(d, 420, 150, 80, 48, 'admin API', 'adminHttp')
    add_box(d, 150, 90, 110, 48, 'Pinia Auth', 'sessionStorage')
    add_box(d, 290, 90, 150, 48, 'Spring Boot API', '/api/*')
    add_arrow(d, 120, 214, 150, 214)
    add_arrow(d, 260, 214, 290, 244)
    add_arrow(d, 260, 214, 290, 174)
    add_arrow(d, 390, 244, 420, 244)
    add_arrow(d, 390, 174, 420, 174)
    add_arrow(d, 460, 220, 365, 138)
    add_arrow(d, 460, 174, 440, 114)
    add_arrow(d, 260, 114, 290, 114)
    return d


def build_backend_diagram():
    d = Drawing(520, 300)
    add_box(d, 20, 220, 110, 48, 'Nginx / 浏览器', '入口流量')
    add_box(d, 160, 220, 100, 48, '安全过滤器', 'SecurityHeaderFilter')
    add_box(d, 290, 220, 100, 48, 'JWT 拦截器', 'JwtAuthInterceptor')
    add_box(d, 410, 220, 90, 48, 'Controller', 'Auth / Site / Admin')
    add_box(d, 410, 140, 90, 48, 'Service', '业务规则')
    add_box(d, 290, 60, 100, 48, 'Mapper/JDBC', 'MyBatis-Plus')
    add_box(d, 160, 60, 100, 48, '缓存', '@Cacheable')
    add_box(d, 30, 60, 100, 48, 'MySQL', '数据存储')
    add_box(d, 410, 60, 90, 48, '异常处理', 'GlobalExceptionHandler')
    add_arrow(d, 130, 244, 160, 244)
    add_arrow(d, 260, 244, 290, 244)
    add_arrow(d, 390, 244, 410, 244)
    add_arrow(d, 455, 220, 455, 188)
    add_arrow(d, 455, 140, 390, 84)
    add_arrow(d, 290, 84, 260, 84)
    add_arrow(d, 160, 84, 130, 84)
    add_arrow(d, 455, 140, 455, 108)
    return d


def build_cover(title: str, md_path: Path, styles):
    info = Table([[Paragraph('源码可追溯技术文档', styles['cover_kicker'])]], colWidths=[170 * mm])
    info.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), colors.HexColor('#eff6ff')),
        ('BOX', (0, 0), (-1, -1), 0.8, colors.HexColor('#bfdbfe')),
        ('LEFTPADDING', (0, 0), (-1, -1), 10),
        ('RIGHTPADDING', (0, 0), (-1, -1), 10),
        ('TOPPADDING', (0, 0), (-1, -1), 10),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 8),
    ]))
    return [
        Spacer(1, 18 * mm),
        info,
        Spacer(1, 10 * mm),
        Paragraph(title, styles['cover_title']),
        Paragraph(f'来源文件：{md_path.name}', styles['cover_meta']),
        Paragraph('生成方式：Python + ReportLab（当前环境下的最小可控方案）', styles['cover_meta']),
        Paragraph('特点：可点击目录、源码锚点清晰、图表内嵌、代码块可读', styles['cover_note']),
        Spacer(1, 8 * mm),
        HRFlowable(width='78%', thickness=1.0, color=colors.HexColor('#cbd5e1'), spaceBefore=4, spaceAfter=4),
        Spacer(1, 4 * mm),
    ]


def build_toc(blocks, styles):
    toc = [Paragraph('目录', styles['h1']), HRFlowable(width='100%', thickness=0.7, color=colors.HexColor('#cbd5e1'), spaceBefore=2, spaceAfter=8)]
    for kind, text, _ in blocks:
        if kind not in {'h1', 'h2'}:
            continue
        anchor = slugify(text)
        style = styles['toc1'] if kind == 'h1' else styles['toc2']
        prefix = '• ' if kind == 'h1' else '◦ '
        toc.append(Paragraph(f'{prefix}<link href="#{anchor}">{escape(text)}</link>', style))
    toc.append(PageBreak())
    return toc


def estimate_col_widths(raw_rows: List[List[str]], available_width: float) -> List[float]:
    col_count = max(len(row) for row in raw_rows)
    weights: List[float] = []
    for col_idx in range(col_count):
        max_len = max((len((row[col_idx] if col_idx < len(row) else '').strip()) for row in raw_rows), default=1)
        weights.append(max(1.0, min(max_len, 30)))
    total = sum(weights) or float(col_count)
    widths = [available_width * weight / total for weight in weights]
    min_width = 22 * mm
    adjusted = widths[:]
    deficit = 0.0
    for i, width in enumerate(adjusted):
        if width < min_width:
            deficit += min_width - width
            adjusted[i] = min_width
    if deficit > 0:
        flexible = [i for i, width in enumerate(adjusted) if width > min_width]
        if flexible:
            shrink = deficit / len(flexible)
            for i in flexible:
                adjusted[i] = max(min_width, adjusted[i] - shrink)
    return adjusted


def build_table(table_lines: List[str], available_width: float, styles):
    raw_rows = []
    for line in table_lines:
        if re.match(r'^\|[\s\-:|]+\|$', line):
            continue
        raw_rows.append([c.strip() for c in line.strip('|').split('|')])
    if not raw_rows:
        return None

    col_widths = estimate_col_widths(raw_rows, available_width)
    rows = []
    for row_idx, raw_row in enumerate(raw_rows):
        style = styles['table_head'] if row_idx == 0 else styles['table_cell']
        padded = raw_row + [''] * (len(col_widths) - len(raw_row))
        rows.append([Paragraph(inline_code(cell), style) for cell in padded])

    tbl = Table(rows, repeatRows=1, colWidths=col_widths)
    tbl.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#e2e8f0')),
        ('TEXTCOLOR', (0, 0), (-1, 0), colors.HexColor('#0f172a')),
        ('GRID', (0, 0), (-1, -1), 0.45, colors.HexColor('#94a3b8')),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.HexColor('#f8fafc')]),
        ('LEFTPADDING', (0, 0), (-1, -1), 6),
        ('RIGHTPADDING', (0, 0), (-1, -1), 6),
        ('TOPPADDING', (0, 0), (-1, -1), 5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 5),
    ]))
    return tbl


def build_section_label(text: str, styles):
    if text not in SECTION_COLORS:
        return Paragraph(escape(text), styles['h3'])
    bg, border, fg = SECTION_COLORS[text]
    tbl = Table([[Paragraph(f'<font color="{fg}"><b>{escape(text)}</b></font>', styles['body'])]], colWidths=['100%'])
    tbl.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), colors.HexColor(bg)),
        ('BOX', (0, 0), (-1, -1), 0.7, colors.HexColor(border)),
        ('LEFTPADDING', (0, 0), (-1, -1), 8),
        ('RIGHTPADDING', (0, 0), (-1, -1), 8),
        ('TOPPADDING', (0, 0), (-1, -1), 5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 4),
    ]))
    return tbl


def build_code_block(code: str, lang: str, styles):
    label = lang or 'code'
    label_bar = Table([[Paragraph(escape(label), styles['code_label'])]], colWidths=['100%'])
    label_bar.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), colors.HexColor('#334155')),
        ('LEFTPADDING', (0, 0), (-1, -1), 8),
        ('RIGHTPADDING', (0, 0), (-1, -1), 8),
        ('TOPPADDING', (0, 0), (-1, -1), 3),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 3),
    ]))
    return KeepTogether([label_bar, Preformatted(code, styles['code'])])


def build_story(title: str, md_path: Path, styles, available_width: float):
    md = md_path.read_text(encoding='utf-8')
    blocks = parse_markdown(md)
    story = build_cover(title, md_path, styles)

    if 'frontend' in md_path.name:
        story.append(Paragraph('架构总览图', styles['h1']))
        story.append(HRFlowable(width='100%', thickness=0.7, color=colors.HexColor('#cbd5e1'), spaceBefore=2, spaceAfter=8))
        story.append(build_frontend_diagram())
        story.append(Paragraph('图示说明：浏览器经由 Vue Router 分流到前台与后台视图；后台链路额外依赖 Pinia 登录态与 adminHttp。', styles['caption']))
        story.append(PageBreak())
    elif 'backend' in md_path.name:
        story.append(Paragraph('请求生命周期图', styles['h1']))
        story.append(HRFlowable(width='100%', thickness=0.7, color=colors.HexColor('#cbd5e1'), spaceBefore=2, spaceAfter=8))
        story.append(build_backend_diagram())
        story.append(Paragraph('图示说明：请求从 Nginx 进入后，依次经过安全头过滤、JWT 拦截、控制器与服务层，再进入 Mapper/JDBC 与缓存。', styles['caption']))
        story.append(PageBreak())

    story.extend(build_toc(blocks, styles))
    table_buffer: List[str] = []

    def flush_table_buffer():
        nonlocal table_buffer
        if not table_buffer:
            return
        table = build_table(table_buffer, available_width, styles)
        if table is not None:
            story.append(table)
            story.append(Spacer(1, 7))
        table_buffer = []

    for kind, text, meta in blocks:
        if kind != 'table':
            flush_table_buffer()
        if kind == 'h1_title':
            continue
        if kind == 'h1':
            anchor = slugify(text)
            story.append(Spacer(1, 2))
            story.append(Paragraph(f'<a name="{anchor}"/>{escape(text)}', styles['h1']))
            story.append(HRFlowable(width='100%', thickness=0.7, color=colors.HexColor('#cbd5e1'), spaceBefore=1, spaceAfter=7))
        elif kind == 'h2':
            anchor = slugify(text)
            story.append(Paragraph(f'<a name="{anchor}"/>{escape(text)}', styles['h2']))
        elif kind == 'h3':
            story.append(build_section_label(text, styles))
            story.append(Spacer(1, 4))
        elif kind == 'p':
            story.append(Paragraph(inline_code(text).replace('\n', '<br/>'), styles['body']))
        elif kind == 'ul':
            story.append(Paragraph(f'• {inline_code(text)}', styles['list']))
        elif kind == 'ol':
            story.append(Paragraph(f'1. {inline_code(text)}', styles['list']))
        elif kind == 'code':
            story.append(Spacer(1, 2))
            story.append(build_code_block(text, meta, styles))
            story.append(Spacer(1, 5))
        elif kind == 'table':
            table_buffer.append(text)
    flush_table_buffer()
    return story


def draw_page_chrome(canvas, doc):
    canvas.saveState()
    canvas.setStrokeColor(colors.HexColor('#cbd5e1'))
    canvas.setLineWidth(0.6)
    canvas.line(16 * mm, A4[1] - 13 * mm, A4[0] - 16 * mm, A4[1] - 13 * mm)
    canvas.line(16 * mm, 14 * mm, A4[0] - 16 * mm, 14 * mm)
    canvas.setFont(ZH_FONT_NAME, 8.8)
    canvas.setFillColor(colors.HexColor('#64748b'))
    canvas.drawString(16 * mm, A4[1] - 10 * mm, doc.title)
    canvas.drawRightString(A4[0] - 16 * mm, 10 * mm, f'第 {doc.page} 页')
    canvas.restoreState()


def render(title: str, src_name: str, out_name: str):
    styles = build_styles()
    src = SOURCE / src_name
    out = OUTPUT / out_name
    doc = SimpleDocTemplate(
        str(out),
        pagesize=A4,
        rightMargin=16 * mm,
        leftMargin=16 * mm,
        topMargin=18 * mm,
        bottomMargin=18 * mm,
        title=title,
        author='Claude Code',
    )
    story = build_story(title, src, styles, doc.width)
    doc.build(story, onFirstPage=draw_page_chrome, onLaterPages=draw_page_chrome)


def main():
    OUTPUT.mkdir(parents=True, exist_ok=True)
    zh_font, mono_font = register_fonts()
    render('前端实现详解', 'frontend.md', 'frontend.pdf')
    render('后端实现详解', 'backend.md', 'backend.pdf')
    print(f'Chinese font: {zh_font}')
    print(f'Monospace font: {mono_font}')
    print('Generated: frontend.pdf, backend.pdf')


if __name__ == '__main__':
    main()
