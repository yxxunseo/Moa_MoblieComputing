import re

svgs = {
    "1": """<svg xmlns="http://www.w3.org/2000/svg" width="75" height="73" viewBox="0 0 75 73" fill="none">
  <path d="M74.5005 34.617C74.5005 53.7354 59.6379 72.6777 40.0005 72.6777C20.3631 72.6777 0 53.7354 0 34.617C0 15.4986 15.9193 0 35.5567 0C55.1941 0 74.5005 15.4986 74.5005 34.617Z" fill="url(#paint0_linear_89_290)"/>
  <path d="M20.5 11.1777L25.7895 16.8747L18 19.1777M46 16.1777L35.9868 18.8444L40.5 23.6777" stroke="#88ACE0" stroke-linecap="round"/>
  <path d="M25 19.6777C25.6142 20.8803 25.8376 21.7996 25.6405 22.6777M23.5 25.6777C24.7269 24.5231 25.428 23.6243 25.6405 22.6777M25.6405 22.6777L34 23.6777" stroke="#88ACE0" stroke-linecap="round"/>
  <path d="M9 21.6777L4.5 26.1777M10.5 22.6777L6 27.1777M49 27.1777L44 32.1777M50.5 28.1777L45.5 33.1777" stroke="#EFA9A0" stroke-linecap="round"/>
  <defs>
    <linearGradient id="paint0_linear_89_290" x1="35.5567" y1="0" x2="35.5567" y2="71.1134" gradientUnits="userSpaceOnUse">
      <stop stop-color="#92BEFD"/>
      <stop offset="1" stop-color="white"/>
    </linearGradient>
  </defs>
</svg>""",
    "2": """<svg xmlns="http://www.w3.org/2000/svg" width="159" height="157" viewBox="0 0 159 157" fill="none">
  <path d="M129.795 80.9512C129.795 108.897 105.863 132.235 79.1951 124.03C54.5789 129.5 28.5952 108.897 28.5952 80.9512C28.5952 53.0057 51.2496 30.3513 79.1951 30.3513C107.141 30.3513 129.795 53.0057 129.795 80.9512Z" fill="url(#paint0_linear_85_154)"/>
  <ellipse cx="59.3658" cy="79.5845" rx="7.52161" ry="2.73513" fill="#FF7F7F" fill-opacity="0.6"/>
  <ellipse cx="101.76" cy="80.9517" rx="7.52161" ry="2.73513" fill="#FF7F7F" fill-opacity="0.6"/>
  <circle cx="61.5323" cy="72.3386" r="6.57856" transform="rotate(-18.4289 61.5323 72.3386)" fill="#FFCB8F"/>
  <circle cx="101.059" cy="73.0363" r="6.57856" transform="rotate(-18.4289 101.059 73.0363)" fill="#FFCB8F"/>
  <path d="M69.644 78.8865C69.644 78.8865 72.0383 83.0531 74.644 83.3865C77.7064 83.7784 78.0666 78.6392 81.144 78.8865C83.9103 79.1088 83.87 83.3079 86.644 83.3865C89.1889 83.4586 92.144 79.8865 92.144 79.8865" stroke="#DDC7AF" stroke-linecap="round"/>
  <path d="M53.644 51.9993L69.1445 60.9998M89.6445 62.4998L108.645 53.9998" stroke="#DDC7AF" stroke-linecap="round"/>
  <defs>
    <linearGradient id="paint0_linear_85_154" x1="79.1951" y1="30.3513" x2="79.1951" y2="125.695" gradientUnits="userSpaceOnUse">
      <stop stop-color="#FFCB8F"/>
      <stop offset="1" stop-color="white"/>
    </linearGradient>
  </defs>
</svg>""",
    "3": """<svg xmlns="http://www.w3.org/2000/svg" width="66" height="65" viewBox="0 0 66 65" fill="none">
  <circle cx="30.5" cy="34.5" r="30.5" fill="url(#paint0_linear_89_287)"/>
  <path d="M23 15.5C26.2841 18.5063 28.4141 19.428 33 19M45.5 24C48.5116 27.7901 50.4259 29.266 54.5 30" stroke="#EFA9A0" stroke-linecap="round"/>
  <path d="M43.5 30.5L30.5 26C30.5 26 30.8407 34.9804 35 36C38.8401 36.9413 43.5 30.5 43.5 30.5Z" fill="#EFA9A0"/>
  <path d="M41.9999 30L33.4999 27C33.4999 27 33.0942 29.2972 36.4999 31C39.5 32.5 41.9999 30 41.9999 30Z" fill="white"/>
  <path d="M16.1176 20V25M14 22.75H18M15.1765 24L17.2941 21.5M15.1765 21.5L17.2941 24" stroke="#FF7F7F" stroke-opacity="0.6" stroke-width="0.5" stroke-linecap="round"/>
  <path d="M53.1176 34V39M51 36.75H55M52.1765 38L54.2941 35.5M52.1765 35.5L54.2941 38" stroke="#FF7F7F" stroke-opacity="0.6" stroke-width="0.5" stroke-linecap="round"/>
  <path d="M18 0L13.5 9.5L30 4L18 0Z" fill="#FAD381"/>
  <path d="M66 28L57 20L61 36.5L66 28Z" fill="#FAD381"/>
  <path d="M18.5 1.5L15.5 8.5L26.5 4.5L18.5 1.5Z" fill="white" fill-opacity="0.54"/>
  <path d="M64.0005 28L58.6828 23.9999L60.1854 33.3717L64.0005 28Z" fill="white" fill-opacity="0.54"/>
  <defs>
    <linearGradient id="paint0_linear_89_287" x1="30.5" y1="4" x2="8.5" y2="56.5" gradientUnits="userSpaceOnUse">
      <stop stop-color="#F9CD71"/>
      <stop offset="1" stop-color="white"/>
    </linearGradient>
  </defs>
</svg>""",
    "4": """<svg xmlns="http://www.w3.org/2000/svg" width="152" height="151" viewBox="0 0 152 151" fill="none">
  <path d="M152 76C152 117.974 153.474 150.5 111.5 150.5C69.5264 150.5 0 117.974 0 76C0 34.0264 34.0264 0 76 0C117.974 0 152 34.0264 152 76Z" fill="url(#paint0_linear_85_223)"/>
  <ellipse cx="73.5787" cy="42.0838" rx="4.80417" ry="5.40966" transform="rotate(8.84107 73.5787 42.0838)" fill="#284478"/>
  <ellipse cx="41.5787" cy="38.0838" rx="4.80417" ry="5.40966" transform="rotate(8.84107 41.5787 38.0838)" fill="#284478"/>
  <ellipse cx="35" cy="44.5" rx="5" ry="3.5" fill="#FF7F7F" fill-opacity="0.6"/>
  <ellipse cx="79" cy="49.5" rx="5" ry="3.5" fill="#FF7F7F" fill-opacity="0.6"/>
  <path d="M72.5 57.258C74.0677 45.2569 42 51.758 42 51.758C42 51.758 47.9271 70.9948 57.5 70.758C65.3785 70.5632 71.4792 65.0726 72.5 57.258Z" fill="#284478"/>
  <path d="M70 64.0002C61.9624 60.8803 56.9202 60.9054 47.5 63.5002C49.3512 66.4732 50.6849 67.8996 53.5 70.0002C56.3593 70.9287 58.0126 70.9205 61 70.5002C65.3426 68.8915 67.4836 67.6604 70 64.0002Z" fill="#FF7F7F"/>
  <path d="M60.5 50C55.6785 49.6815 53.2721 50.0545 49.1785 50.5214C45.085 50.9884 52.6011 53.2806 55 53C56.8946 52.7783 65.3215 50.3185 60.5 50Z" fill="white"/>
  <defs>
    <linearGradient id="paint0_linear_85_223" x1="76.0032" y1="3.28087e-06" x2="185" y2="215" gradientUnits="userSpaceOnUse">
      <stop stop-color="#77AAFD"/>
      <stop offset="1" stop-color="#66FFC2"/>
    </linearGradient>
  </defs>
</svg>""",
    "5": """<svg xmlns="http://www.w3.org/2000/svg" width="68" height="94" viewBox="0 0 68 94" fill="none">
  <path d="M68 34C68 52.7777 37.2777 94 18.5 94C-0.277681 94 0 52.7777 0 34C0 15.2223 15.2223 0 34 0C52.7777 0 68 15.2223 68 34Z" fill="url(#paint0_linear_135_235)"/>
  <defs>
    <linearGradient id="paint0_linear_135_235" x1="34" y1="0" x2="34" y2="94" gradientUnits="userSpaceOnUse">
      <stop stop-color="#F1A5A5"/>
      <stop offset="1" stop-color="white"/>
    </linearGradient>
  </defs>
</svg>""",
    "6": """<svg xmlns="http://www.w3.org/2000/svg" width="92" height="97" viewBox="0 0 92 97" fill="none">
  <path d="M91.5 78.5C91.5 104.457 72.9574 94 47 94C21.0426 94 0 72.9574 0 47C0 21.0426 21.0426 0 47 0C72.9574 0 91.5 52.5426 91.5 78.5Z" fill="url(#paint0_linear_135_234)"/>
  <defs>
    <linearGradient id="paint0_linear_135_234" x1="45.75" y1="0" x2="45.75" y2="96.0821" gradientUnits="userSpaceOnUse">
      <stop stop-color="#D478E4"/>
      <stop offset="1" stop-color="#008686"/>
    </linearGradient>
  </defs>
</svg>""",
    "7": """<svg xmlns="http://www.w3.org/2000/svg" width="258" height="137" viewBox="0 0 258 137" fill="none">
  <path d="M250.417 129.149C250.417 129.149 170.056 115.324 117.519 95.7787C99.1675 88.9512 82.9628 79.7719 69.0186 69.9084C28.9071 41.5354 7.50075 7.50113 7.50075 7.50113" stroke="url(#paint0_linear_85_226)" stroke-width="15" stroke-linecap="round"/>
  <defs>
    <linearGradient id="paint0_linear_85_226" x1="166.991" y1="34.7232" x2="89.5322" y2="103.158" gradientUnits="userSpaceOnUse">
      <stop stop-color="#E6D3AB"/>
      <stop offset="1" stop-color="#D1BB8E"/>
    </linearGradient>
  </defs>
</svg>""",
    "8": """<svg xmlns="http://www.w3.org/2000/svg" width="72" height="56" viewBox="0 0 72 56" fill="none">
  <path d="M47.5308 55L1.03076 22L53.5308 0.5M1.03076 22L59.5308 9.5M1.03076 22L65.5308 37M1.03076 22L56.5308 46M1.03076 22L70.5308 20" stroke="url(#paint0_linear_85_229)" stroke-linecap="round"/>
  <defs>
    <linearGradient id="paint0_linear_85_229" x1="0.530761" y1="22.5" x2="34.5308" y2="21" gradientUnits="userSpaceOnUse">
      <stop stop-color="#E6D3AB"/>
      <stop offset="1" stop-color="#DBC79C"/>
    </linearGradient>
  </defs>
</svg>"""
}

def to_color(s):
    if s == "white": return "Color.White"
    if s.startswith("#") and len(s) == 7:
        return f"Color(0xFF{s[1:].upper()})"
    return "Color.Black"

output = []
output.append("package com.example.moa_project.ui.splash\n")
output.append("import androidx.compose.foundation.Canvas")
output.append("import androidx.compose.runtime.Composable")
output.append("import androidx.compose.ui.Modifier")
output.append("import androidx.compose.ui.geometry.Offset")
output.append("import androidx.compose.ui.geometry.Size")
output.append("import androidx.compose.ui.graphics.Brush")
output.append("import androidx.compose.ui.graphics.Color")
output.append("import androidx.compose.ui.graphics.StrokeCap")
output.append("import androidx.compose.ui.graphics.drawscope.Stroke")
output.append("import androidx.compose.ui.graphics.drawscope.scale")
output.append("import androidx.compose.ui.graphics.drawscope.rotate")
output.append("import androidx.core.graphics.PathParser")
output.append("import androidx.compose.ui.graphics.asComposePath")
output.append("import androidx.compose.ui.graphics.Path")
output.append("")

path_counter = 0

for name, svg in svgs.items():
    output.append(f"@Composable\nfun DrawSplashSvg{name}(modifier: Modifier = Modifier) {{")
    
    m = re.search(r'width="(\d+)" height="(\d+)"', svg)
    w = m.group(1) if m else "100"
    h = m.group(2) if m else "100"
    
    output.append(f"    Canvas(modifier = modifier) {{")
    output.append(f"        val scaleX = size.width / {w}f")
    output.append(f"        val scaleY = size.height / {h}f")
    output.append(f"        scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset.Zero) {{")
    
    gradients = {}
    for g in re.finditer(r'<linearGradient id="([^"]+)" x1="([^"]+)" y1="([^"]+)" x2="([^"]+)" y2="([^"]+)"[^>]*>\s*<stop stop-color="([^"]+)"(?: stop-opacity="([^"]+)")?/>\s*<stop offset="1" stop-color="([^"]+)"(?: stop-opacity="([^"]+)")?/>\s*</linearGradient>', svg):
        id, x1, y1, x2, y2, c1, op1, c2, op2 = g.groups()
        c1_str = to_color(c1)
        c2_str = to_color(c2)
        if op1: c1_str += f".copy(alpha={op1}f)"
        if op2: c2_str += f".copy(alpha={op2}f)"
        gradients[id] = f"Brush.linearGradient(listOf({c1_str}, {c2_str}), Offset({x1}f, {y1}f), Offset({x2}f, {y2}f))"
    
    items = re.findall(r'<(path|circle|ellipse) ([^>]+)/>', svg)
    for tag, attrs in items:
        attr_dict = dict(re.findall(r'([a-zA-Z0-9-]+)="([^"]+)"', attrs))
        
        brush_val = "null"
        color_val = "Color.Black"
        style_val = "androidx.compose.ui.graphics.drawscope.Fill"
        
        fill = attr_dict.get("fill", "black")
        if fill == "none": fill = None
        stroke = attr_dict.get("stroke", None)
        stroke_width = attr_dict.get("stroke-width", "1")
        stroke_linecap = attr_dict.get("stroke-linecap", "butt")
        fill_opacity = float(attr_dict.get("fill-opacity", "1.0"))
        stroke_opacity = float(attr_dict.get("stroke-opacity", "1.0"))
        
        if fill:
            if fill.startswith("url(#"):
                grad_id = fill[5:-1]
                brush_val = gradients.get(grad_id, "null")
            else:
                color_val = to_color(fill)
                if fill_opacity != 1.0: color_val += f".copy(alpha={fill_opacity}f)"
            
        if stroke:
            style_cap = "StrokeCap.Round" if stroke_linecap == "round" else "StrokeCap.Butt"
            style_val = f"Stroke(width = {stroke_width}f, cap = {style_cap})"
            if stroke.startswith("url(#"):
                grad_id = stroke[5:-1]
                brush_val = gradients.get(grad_id, "null")
            else:
                color_val = to_color(stroke)
                if stroke_opacity != 1.0: color_val += f".copy(alpha={stroke_opacity}f)"
        
        if tag == "path":
            path_counter += 1
            var_name = f"path_{path_counter}"
            d = attr_dict["d"]
            output.append(f"            val {var_name} = PathParser.createPathFromPathData(\"{d}\").asComposePath()")
            if brush_val != "null":
                output.append(f"            drawPath({var_name}, brush = {brush_val}, style = {style_val})")
            else:
                output.append(f"            drawPath({var_name}, color = {color_val}, style = {style_val})")
                
        elif tag == "circle":
            cx = float(attr_dict["cx"]); cy = float(attr_dict["cy"]); r = float(attr_dict["r"])
            transform = attr_dict.get("transform", "")
            draw_stmt = []
            if transform.startswith("rotate("):
                m_rot = re.search(r'rotate\(([-\d.]+) ([-\d.]+) ([-\d.]+)\)', transform)
                if m_rot:
                    deg, rx, ry = m_rot.groups()
                    draw_stmt.append(f"            rotate(degrees = {deg}f, pivot = Offset({rx}f, {ry}f)) {{")
            
            if brush_val != "null": draw_stmt.append(f"                drawCircle(brush = {brush_val}, radius = {r}f, center = Offset({cx}f, {cy}f))")
            else: draw_stmt.append(f"                drawCircle(color = {color_val}, radius = {r}f, center = Offset({cx}f, {cy}f))")
                
            if transform.startswith("rotate("): draw_stmt.append("            }")
            output.extend(draw_stmt)
            
        elif tag == "ellipse":
            cx = float(attr_dict["cx"]); cy = float(attr_dict["cy"]); rx = float(attr_dict["rx"]); ry = float(attr_dict["ry"])
            transform = attr_dict.get("transform", "")
            draw_stmt = []
            if transform.startswith("rotate("):
                m_rot = re.search(r'rotate\(([-\d.]+) ([-\d.]+) ([-\d.]+)\)', transform)
                if m_rot:
                    deg, rotx, roty = m_rot.groups()
                    draw_stmt.append(f"            rotate(degrees = {deg}f, pivot = Offset({rotx}f, {roty}f)) {{")
            
            if brush_val != "null": draw_stmt.append(f"                drawOval(brush = {brush_val}, topLeft = Offset({cx - rx}f, {cy - ry}f), size = Size({rx*2}f, {ry*2}f))")
            else: draw_stmt.append(f"                drawOval(color = {color_val}, topLeft = Offset({cx - rx}f, {cy - ry}f), size = Size({rx*2}f, {ry*2}f))")
                
            if transform.startswith("rotate("): draw_stmt.append("            }")
            output.extend(draw_stmt)
            
    # Add face details for 5 and 6 since they are missing in SVG!
    if name == "5":
        # Pink character: needs two angry eyebrows, two eyes, mouth.
        # Draw on top of the character. The pink character is 68x94.
        # It's facing right/down.
        output.append("            // Added missing facial details for Pink character")
        output.append("            drawCircle(color = Color(0xFF284478), radius = 2.5f, center = Offset(30f, 55f)) // Left eye")
        output.append("            drawCircle(color = Color(0xFF284478), radius = 2.5f, center = Offset(45f, 60f)) // Right eye")
        output.append("            drawLine(color = Color(0xFF284478), start = Offset(24f, 50f), end = Offset(32f, 52f), strokeWidth = 1.5f, cap = StrokeCap.Round) // Left eyebrow")
        output.append("            drawLine(color = Color(0xFF284478), start = Offset(51f, 55f), end = Offset(43f, 57f), strokeWidth = 1.5f, cap = StrokeCap.Round) // Right eyebrow")
        output.append("            drawArc(color = Color(0xFF284478), startAngle = 0f, sweepAngle = 180f, useCenter = true, topLeft = Offset(33f, 58f), size = Size(10f, 6f)) // Mouth")

    if name == "6":
        # Purple character: needs two eyes. It's behind the blue one, peeking over.
        # Size 92x97.
        output.append("            // Added missing facial details for Purple character")
        output.append("            drawCircle(color = Color(0xFF284478), radius = 3f, center = Offset(40f, 35f))")
        output.append("            drawCircle(color = Color(0xFF284478), radius = 3f, center = Offset(60f, 40f))")

    output.append(f"        }}")
    output.append(f"    }}")
    output.append(f"}}")
    output.append("")

with open("app/src/main/java/com/example/moa_project/ui/splash/WitchGraphics.kt", "w") as f:
    f.write("\n".join(output))
