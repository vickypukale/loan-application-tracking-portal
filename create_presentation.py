import os
from pptx import Presentation
from pptx.util import Inches, Pt
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from pptx.enum.shapes import MSO_SHAPE

def create_deck():
    prs = Presentation()
    prs.slide_width = Inches(13.333)
    prs.slide_height = Inches(7.5)
    blank_layout = prs.slide_layouts[6]

    # Colors
    NAVY = RGBColor(15, 30, 54)        # #0F1E36 (Dark theme)
    WHITE = RGBColor(255, 255, 255)
    LIGHT_BG = RGBColor(245, 247, 250) # #F5F7FA
    ROYAL_BLUE = RGBColor(26, 86, 164) # #1A56A4
    TEAL = RGBColor(0, 168, 150)       # #00A896
    GOLD = RGBColor(235, 130, 60)      # #EB823C
    DARK_TEXT = RGBColor(30, 41, 59)   # #1E293B
    MUTED_TEXT = RGBColor(100, 116, 139) # #64748B
    CARD_BG = RGBColor(255, 255, 255)
    CARD_BORDER = RGBColor(226, 232, 240)
    SUCCESS_GREEN = RGBColor(22, 163, 74)
    DANGER_RED = RGBColor(220, 38, 38)
    PURPLE = RGBColor(126, 58, 242)

    def set_slide_background(slide, color):
        background = slide.background
        fill = background.fill
        fill.solid()
        fill.fore_color.rgb = color

    def add_header(slide, title_text, category_text="DEVOPS CAPSTONE PROJECT"):
        # Category pill
        cat_box = slide.shapes.add_textbox(Inches(0.8), Inches(0.4), Inches(11.7), Inches(0.35))
        tf_cat = cat_box.text_frame
        tf_cat.word_wrap = True
        p_cat = tf_cat.paragraphs[0]
        p_cat.text = category_text.upper()
        p_cat.font.size = Pt(10)
        p_cat.font.bold = True
        p_cat.font.color.rgb = ROYAL_BLUE

        # Title
        title_box = slide.shapes.add_textbox(Inches(0.8), Inches(0.65), Inches(11.7), Inches(0.7))
        tf_title = title_box.text_frame
        tf_title.word_wrap = True
        p_title = tf_title.paragraphs[0]
        p_title.text = title_text
        p_title.font.size = Pt(22)
        p_title.font.bold = True
        p_title.font.color.rgb = NAVY

    def add_card(slide, left, top, width, height, bg_color=CARD_BG, border_color=CARD_BORDER):
        shape = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left, top, width, height)
        shape.fill.solid()
        shape.fill.fore_color.rgb = bg_color
        shape.line.color.rgb = border_color
        shape.line.width = Pt(1.2)
        return shape

    # ==========================================================
    # SLIDE 1: Title Slide (Dark Theme)
    # ==========================================================
    s1 = prs.slides.add_slide(blank_layout)
    set_slide_background(s1, NAVY)

    # Accent bar
    top_bar = s1.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0), Inches(0), Inches(13.333), Inches(0.15))
    top_bar.fill.solid()
    top_bar.fill.fore_color.rgb = TEAL
    top_bar.line.fill.background()

    # Category Badge
    pill = s1.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(1.0), Inches(1.1), Inches(3.2), Inches(0.45))
    pill.fill.solid()
    pill.fill.fore_color.rgb = ROYAL_BLUE
    pill.line.fill.background()
    p_pill = pill.text_frame.paragraphs[0]
    p_pill.text = "CAPSTONE DEVOPS PORTFOLIO"
    p_pill.alignment = PP_ALIGN.CENTER
    p_pill.font.size = Pt(11)
    p_pill.font.bold = True
    p_pill.font.color.rgb = WHITE

    # Project Title
    t_box = s1.shapes.add_textbox(Inches(1.0), Inches(1.8), Inches(11.3), Inches(1.8))
    tf = t_box.text_frame
    tf.word_wrap = True
    p1 = tf.paragraphs[0]
    p1.text = "Secure CI/CD Pipeline for a\nLoan Application Tracking Portal"
    p1.font.size = Pt(36)
    p1.font.bold = True
    p1.font.color.rgb = WHITE

    p2 = tf.add_paragraph()
    p2.text = "Enterprise-Grade Automation with Jenkins, Selenium, Docker, Ansible & SBI Domain Integration"
    p2.font.size = Pt(16)
    p2.font.color.rgb = RGBColor(148, 163, 184)
    p2.space_before = Pt(12)

    # Candidate Profile Box
    c_card = s1.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(1.0), Inches(4.3), Inches(11.333), Inches(1.7))
    c_card.fill.solid()
    c_card.fill.fore_color.rgb = RGBColor(23, 42, 69)
    c_card.line.color.rgb = RGBColor(51, 65, 85)

    c_box = s1.shapes.add_textbox(Inches(1.3), Inches(4.45), Inches(10.7), Inches(1.4))
    ctf = c_box.text_frame
    ctf.word_wrap = True
    
    cp1 = ctf.paragraphs[0]
    cp1.text = "CANDIDATE & EXAMINATION CREDENTIALS"
    cp1.font.size = Pt(11)
    cp1.font.bold = True
    cp1.font.color.rgb = GOLD

    cp2 = ctf.add_paragraph()
    cp2.text = "Lead Engineer: Vicky Vilas Pukale  |  Roll Number: 23102A0033  |  Release: v2.0.0-final"
    cp2.font.size = Pt(14)
    cp2.font.bold = True
    cp2.font.color.rgb = WHITE
    cp2.space_before = Pt(6)

    cp3 = ctf.add_paragraph()
    cp3.text = "Tech Stack: Java 17 LTS  •  Spring Boot 3.2  •  Jenkinsfile  •  Docker Multi-Stage  •  Ansible IaC  •  Persistent H2 DB"
    cp3.font.size = Pt(12)
    cp3.font.color.rgb = TEAL
    cp3.space_before = Pt(6)

    # Footer note
    f_box = s1.shapes.add_textbox(Inches(1.0), Inches(6.3), Inches(11.3), Inches(0.5))
    f_box.text_frame.paragraphs[0].text = "State Bank of India (SBI) Retail Lending Underwriting Standard  |  15-Task Complete DevOps Delivery"
    f_box.text_frame.paragraphs[0].font.size = Pt(11)
    f_box.text_frame.paragraphs[0].font.color.rgb = RGBColor(100, 116, 139)

    # ==========================================================
    # SLIDE 2: Executive Summary & Project Problem Statement
    # ==========================================================
    s2 = prs.slides.add_slide(blank_layout)
    set_slide_background(s2, LIGHT_BG)
    add_header(s2, "Executive Overview & Problem Statement")

    # Card 1: Problem
    add_card(s2, Inches(0.8), Inches(1.5), Inches(3.6), Inches(5.3))
    b1 = s2.shapes.add_textbox(Inches(1.0), Inches(1.7), Inches(3.2), Inches(4.9))
    tf1 = b1.text_frame
    tf1.word_wrap = True
    p = tf1.paragraphs[0]
    p.text = "Operational Friction"
    p.font.size = Pt(16)
    p.font.bold = True
    p.font.color.rgb = DANGER_RED

    bullets_1 = [
        "Opaque Processing: Retail borrowers have zero real-time visibility into their application lifecycle.",
        "Manual Underwriting Delays: Offline evaluation of CIBIL credit scores and debt ratios takes 5-7 days.",
        "Configuration Drift: Inconsistent server setups cause 'it works on my machine' production bugs.",
        "Manual Deployment Risks: High downtime risk without automated health checks or rollback capabilities."
    ]
    for b in bullets_1:
        p = tf1.add_paragraph()
        p.text = "• " + b
        p.font.size = Pt(12)
        p.font.color.rgb = DARK_TEXT
        p.space_before = Pt(10)

    # Card 2: Solution
    add_card(s2, Inches(4.8), Inches(1.5), Inches(3.8), Inches(5.3))
    b2 = s2.shapes.add_textbox(Inches(5.0), Inches(1.7), Inches(3.4), Inches(4.9))
    tf2 = b2.text_frame
    tf2.word_wrap = True
    p = tf2.paragraphs[0]
    p.text = "The DevOps Solution"
    p.font.size = Pt(16)
    p.font.bold = True
    p.font.color.rgb = ROYAL_BLUE

    bullets_2 = [
        "Spring Boot Web Portal: Responsive UI for loan intake, KPI analytics, status drill-down, and exception queue.",
        "Zero-Touch CI/CD: Declarative Jenkins pipeline triggering build, test, and container deploy on Git push.",
        "Headless Selenium Gate: Automated UI regression suite preventing defective code from reaching production.",
        "Idempotent Ansible IaC: Automated server configuration with verified zero-drift guarantee.",
        "Sub-15s Rollback: Immediate container replacement upon health failure."
    ]
    for b in bullets_2:
        p = tf2.add_paragraph()
        p.text = "• " + b
        p.font.size = Pt(12)
        p.font.color.rgb = DARK_TEXT
        p.space_before = Pt(10)

    # Card 3: Key Outcomes
    add_card(s2, Inches(8.9), Inches(1.5), Inches(3.6), Inches(5.3))
    b3 = s2.shapes.add_textbox(Inches(9.1), Inches(1.7), Inches(3.2), Inches(4.9))
    tf3 = b3.text_frame
    tf3.word_wrap = True
    p = tf3.paragraphs[0]
    p.text = "Key Project Outcomes"
    p.font.size = Pt(16)
    p.font.bold = True
    p.font.color.rgb = SUCCESS_GREEN

    bullets_3 = [
        "19/19 Tests Passing: 100% pass rate across JUnit 5 unit tests and Selenium E2E journeys.",
        "Port Isolation Matrix: Zero port conflict (Port 8080 Jenkins vs Port 8081 App).",
        "Persistent Local Storage: H2 file-based persistent DB storing loans, events, and KYC documents.",
        "SBI 4-Pillar Integration: Real-world automated underwriting (CIBIL >= 750, FOIR <= 50%, Age 21-60).",
        "DORA Excellence: Sub-5 min lead time, 0% change failure rate."
    ]
    for b in bullets_3:
        p = tf3.add_paragraph()
        p.text = "• " + b
        p.font.size = Pt(12)
        p.font.color.rgb = DARK_TEXT
        p.space_before = Pt(10)

    # ==========================================================
    # SLIDE 3: End-to-End DevOps Architecture
    # ==========================================================
    s3 = prs.slides.add_slide(blank_layout)
    set_slide_background(s3, LIGHT_BG)
    add_header(s3, "End-to-End CI/CD Pipeline & Infrastructure Architecture")

    # Flow Stages (5 Cards across)
    stages = [
        ("1. Git Workstation", "Version Control", [
            "Feature branching",
            "Commit & Push to main",
            "Release tag v2.0.0",
            "Signed commits"
        ], ROYAL_BLUE),
        ("2. Jenkins CI Server", "Pipeline as Code", [
            "Port 8080 Isolation",
            "SCM Checkout",
            "Maven Compilation",
            "JUnit 5 Unit Tests"
        ], PURPLE),
        ("3. Quality Gate", "Automated UI Testing", [
            "Headless Chrome",
            "Selenium WebDriver",
            "6 E2E User Journeys",
            "Screenshot on failure"
        ], DANGER_RED),
        ("4. Docker Engine", "Containerization", [
            "Multi-stage Dockerfile",
            "Alpine JRE 17 runtime",
            "Non-root 'appuser'",
            "Tagged image build"
        ], TEAL),
        ("5. Target Host", "Ansible & Runtime", [
            "Port 8081 Host Mapping",
            "Idempotent Provisioning",
            "Persistent Local H2 DB",
            "Actuator /health Check"
        ], SUCCESS_GREEN)
    ]

    card_w = Inches(2.25)
    gap = Inches(0.18)
    start_x = Inches(0.8)

    for i, (title, subtitle, items, color) in enumerate(stages):
        x = start_x + i * (card_w + gap)
        # Card shape
        card = add_card(s3, x, Inches(1.5), card_w, Inches(4.5))
        
        # Color bar on top
        bar = s3.shapes.add_shape(MSO_SHAPE.RECTANGLE, x, Inches(1.5), card_w, Inches(0.12))
        bar.fill.solid()
        bar.fill.fore_color.rgb = color
        bar.line.fill.background()

        tb = s3.shapes.add_textbox(x + Inches(0.1), Inches(1.7), card_w - Inches(0.2), Inches(4.1))
        tf = tb.text_frame
        tf.word_wrap = True

        p1 = tf.paragraphs[0]
        p1.text = title
        p1.font.size = Pt(13)
        p1.font.bold = True
        p1.font.color.rgb = NAVY

        p2 = tf.add_paragraph()
        p2.text = subtitle
        p2.font.size = Pt(10)
        p2.font.bold = True
        p2.font.color.rgb = color
        p2.space_before = Pt(3)

        for it in items:
            p = tf.add_paragraph()
            p.text = "• " + it
            p.font.size = Pt(10.5)
            p.font.color.rgb = DARK_TEXT
            p.space_before = Pt(8)

    # Bottom Architecture Summary Callout
    bot_card = add_card(s3, Inches(0.8), Inches(6.2), Inches(11.733), Inches(0.9), bg_color=NAVY)
    b_box = s3.shapes.add_textbox(Inches(1.0), Inches(6.25), Inches(11.3), Inches(0.8))
    btf = b_box.text_frame
    btf.word_wrap = True
    bp = btf.paragraphs[0]
    bp.text = "🔒 Zero-Touch Automated Flow: Code Commit  ──►  Build & Test  ──►  Selenium Quality Gate  ──►  Docker Packaging  ──►  Ansible Baseline  ──►  Port 8081 Deployment"
    bp.font.size = Pt(12)
    bp.font.bold = True
    bp.font.color.rgb = WHITE

    # ==========================================================
    # SLIDE 4: Port Isolation Matrix & Network Architecture
    # ==========================================================
    s4 = prs.slides.add_slide(blank_layout)
    set_slide_background(s4, LIGHT_BG)
    add_header(s4, "Port Isolation Matrix & Host Network Architecture")

    # Left: Explanation Card
    add_card(s4, Inches(0.8), Inches(1.5), Inches(5.6), Inches(5.4))
    lb = s4.shapes.add_textbox(Inches(1.0), Inches(1.7), Inches(5.2), Inches(5.0))
    ltf = lb.text_frame
    ltf.word_wrap = True
    
    lp = ltf.paragraphs[0]
    lp.text = "Port Contention Prevention Strategy"
    lp.font.size = Pt(16)
    lp.font.bold = True
    lp.font.color.rgb = ROYAL_BLUE

    points_s4 = [
        "Contention Hazard: In naive CI/CD environments, Jenkins and the application both default to port 8080, causing catastrophic deployment failures.",
        "Dedicated Port 8080: Reserved exclusively for the Jenkins CI/CD Controller. Application services are strictly barred from binding to 8080.",
        "Dedicated Port 8081: Assigned exclusively to the Loan Application Tracking Portal across all configuration layers:",
        "   - Spring Boot: server.port=8081 in application.properties",
        "   - Jenkinsfile: TARGET_PORT=8081 environment variable",
        "   - Docker Runtime: -p 8081:8081 host-to-container port mapping",
        "   - Ansible Security: UFW firewall rule permitting 8081/tcp",
        "SSH Administration: Port 22 configured with unprivileged service account loanapp."
    ]
    for pt in points_s4:
        p = ltf.add_paragraph()
        p.text = pt
        p.font.size = Pt(11)
        p.font.color.rgb = DARK_TEXT
        p.space_before = Pt(8)

    # Right: Table & Visuals
    add_card(s4, Inches(6.8), Inches(1.5), Inches(5.733), Inches(5.4))
    rb = s4.shapes.add_textbox(Inches(7.0), Inches(1.7), Inches(5.3), Inches(5.0))
    rtf = rb.text_frame
    rtf.word_wrap = True

    rp = rtf.paragraphs[0]
    rp.text = "Network Topology & Port Matrix"
    rp.font.size = Pt(16)
    rp.font.bold = True
    rp.font.color.rgb = NAVY

    rows = [
        ("Port 8080 (TCP)", "Jenkins Controller", "CI/CD Orchestrator", "Dedicated to Jenkins"),
        ("Port 8081 (TCP)", "Loan Application Portal", "Spring Boot MVC Web App", "Active Production"),
        ("Port 8081 (TCP)", "/actuator/health", "Health & Smoke Telemetry", "HTTP 200 {status: UP}"),
        ("Port 8081 (TCP)", "/h2-console", "Persistent Database Console", "Local Persistent DB"),
        ("Port 22 (TCP)", "SSH Server Access", "Ansible Automation User", "Restricted Key Auth")
    ]
    for port, service, role, status in rows:
        p = rtf.add_paragraph()
        p.text = f"• {port} — {service}"
        p.font.size = Pt(12)
        p.font.bold = True
        p.font.color.rgb = ROYAL_BLUE
        p.space_before = Pt(10)

        p_sub = rtf.add_paragraph()
        p_sub.text = f"   Role: {role}  |  Status: {status}"
        p_sub.font.size = Pt(10)
        p_sub.font.color.rgb = MUTED_TEXT

    # ==========================================================
    # SLIDE 5: Full Technology Stack Breakdown
    # ==========================================================
    s5 = prs.slides.add_slide(blank_layout)
    set_slide_background(s5, LIGHT_BG)
    add_header(s5, "Comprehensive Technology Stack Breakdown")

    stack_cards = [
        ("Application Layer", ROYAL_BLUE, [
            ("Java OpenJDK 17 LTS", "Modern object-oriented runtime with LTS stability"),
            ("Spring Boot 3.2.3", "Embedded Tomcat, dependency injection, MVC controllers"),
            ("Thymeleaf & Bootstrap 5", "Server-rendered responsive UI with deterministic IDs"),
            ("Spring Data JPA & Hibernate", "Declarative ORM persistence and entity mappings")
        ]),
        ("Database & Storage", TEAL, [
            ("Embedded H2 Database", "File-based persistent storage at ./data/loandb.mv.db"),
            ("H2 Web Management Console", "Direct table queries and transaction monitoring"),
            ("KYC Secure Document Vault", "UUID-based file storage outside web root at uploads/"),
            ("Automatic DDL Migration", "Zero-friction schema evolution on startup")
        ]),
        ("Quality Assurance", PURPLE, [
            ("JUnit 5 Platform", "Modern unit & integration regression test framework"),
            ("Selenium WebDriver 4.18", "Headless Chrome browser E2E test execution"),
            ("ScreenshotOnFailureExtension", "Auto-captures PNG snapshots on UI test failure"),
            ("Spring Boot Test & MockMvc", "Full application context integration testing")
        ]),
        ("DevOps & Infrastructure", NAVY, [
            ("Jenkins 2.440+ LTS", "Pipeline as Code orchestrating 7-stage CI/CD"),
            ("Docker Multi-Stage", "Minimal Alpine JRE 17 container with non-root user"),
            ("Ansible 2.15+ (IaC)", "Idempotent YAML playbooks managing host baseline"),
            ("Spring Boot Actuator", "Production telemetry, health probes, and metrics")
        ])
    ]

    card_w = Inches(2.75)
    gap = Inches(0.24)
    start_x = Inches(0.8)

    for i, (cat_title, color, items) in enumerate(stack_cards):
        x = start_x + i * (card_w + gap)
        add_card(s5, x, Inches(1.5), card_w, Inches(5.4))
        
        # Color bar
        bar = s5.shapes.add_shape(MSO_SHAPE.RECTANGLE, x, Inches(1.5), card_w, Inches(0.12))
        bar.fill.solid()
        bar.fill.fore_color.rgb = color
        bar.line.fill.background()

        tb = s5.shapes.add_textbox(x + Inches(0.15), Inches(1.7), card_w - Inches(0.3), Inches(5.0))
        tf = tb.text_frame
        tf.word_wrap = True

        p1 = tf.paragraphs[0]
        p1.text = cat_title
        p1.font.size = Pt(14)
        p1.font.bold = True
        p1.font.color.rgb = color

        for name, desc in items:
            pn = tf.add_paragraph()
            pn.text = "• " + name
            pn.font.size = Pt(11)
            pn.font.bold = True
            pn.font.color.rgb = DARK_TEXT
            pn.space_before = Pt(10)

            pd = tf.add_paragraph()
            pd.text = "   " + desc
            pd.font.size = Pt(9.5)
            pd.font.color.rgb = MUTED_TEXT

    # ==========================================================
    # SLIDE 6: 15-Task DevOps Lifecycle Roadmap
    # ==========================================================
    s6 = prs.slides.add_slide(blank_layout)
    set_slide_background(s6, LIGHT_BG)
    add_header(s6, "15-Task DevOps Implementation Roadmap (Vicky Pukale)")

    phases = [
        ("Phase I: Application Core (Tasks 1-5)", ROYAL_BLUE, [
            "Task 1: Git repository initialization & Maven project skeleton",
            "Task 2: Branching strategy (main, develop, feature branches)",
            "Task 3: Loan intake form with auto-generated LOAN-XXXX IDs",
            "Task 4: Interactive dashboard, 4 KPI counters, status drill-down",
            "Task 5: Dedicated /alerts queue for flagged/rejected loans"
        ]),
        ("Phase II: Automated Testing (Tasks 6-9)", PURPLE, [
            "Task 6: JUnit 5 unit & integration tests for service layer",
            "Task 7: Jenkins controller setup & Declarative Jenkinsfile",
            "Task 8: Selenium WebDriver headless test suite (4 Journeys)",
            "Task 9: ScreenshotOnFailure quality gate & deliberate defect injection"
        ]),
        ("Phase III: Docker & Ansible (Tasks 10-13)", TEAL, [
            "Task 10: Multi-stage Dockerfile with non-root security user",
            "Task 11: Continuous container deployment & image tagging",
            "Task 12: Target node baseline specification & inventory",
            "Task 13: Ansible automated provisioning with 100% idempotency proof"
        ]),
        ("Phase IV: Reliability & SBI Upgrade (Tasks 14-15)", SUCCESS_GREEN, [
            "Task 14: Actuator health checks & sub-15s disaster recovery rollback",
            "Task 15: Technical documentation, DORA metrics, & viva defense",
            "SBI Enhancement: 4-Pillar risk engine & persistent local H2 DB",
            "Final Delivery: 19/19 tests passing, production-ready release v2.0.0"
        ])
    ]

    card_w = Inches(2.75)
    gap = Inches(0.24)
    start_x = Inches(0.8)

    for i, (phase_title, color, items) in enumerate(phases):
        x = start_x + i * (card_w + gap)
        add_card(s6, x, Inches(1.5), card_w, Inches(5.4))
        
        bar = s6.shapes.add_shape(MSO_SHAPE.RECTANGLE, x, Inches(1.5), card_w, Inches(0.12))
        bar.fill.solid()
        bar.fill.fore_color.rgb = color
        bar.line.fill.background()

        tb = s6.shapes.add_textbox(x + Inches(0.12), Inches(1.7), card_w - Inches(0.24), Inches(5.0))
        tf = tb.text_frame
        tf.word_wrap = True

        p1 = tf.paragraphs[0]
        p1.text = phase_title
        p1.font.size = Pt(12)
        p1.font.bold = True
        p1.font.color.rgb = color

        for item in items:
            p = tf.add_paragraph()
            p.text = "✓ " + item
            p.font.size = Pt(10)
            p.font.color.rgb = DARK_TEXT
            p.space_before = Pt(8)

    # ==========================================================
    # SLIDE 7: Application Features & User Experience
    # ==========================================================
    s7 = prs.slides.add_slide(blank_layout)
    set_slide_background(s7, LIGHT_BG)
    add_header(s7, "Loan Portal User Experience & Core Functional Capabilities")

    features = [
        ("Borrower Application Intake", ROYAL_BLUE, [
            "Multi-step intake form with real-time field validation",
            "PAN Card format enforcement: [A-Z]{5}[0-9]{4}[A-Z]{1}",
            "Auto-generates sequential IDs (LOAN-1001, LOAN-1002)",
            "Instant FOIR live preview as income/EMI values are typed"
        ]),
        ("Executive KPI Dashboard", TEAL, [
            "Total Applications counter",
            "In Intake / Review counter (SUBMITTED status)",
            "Approved Loans counter (APPROVED / SANCTIONED)",
            "Rejected / Exceptions counter (REJECTED / HIGH RISK)"
        ]),
        ("Real-Time Search & Filtering", PURPLE, [
            "Live substring search across Application ID & Borrower Name",
            "One-click Reset filter restoring full workload overview",
            "SBI Risk Tag badge displayed on each record",
            "Instant drill-down button navigating to applicant profile"
        ]),
        ("Status Drill-Down & Timeline", SUCCESS_GREEN, [
            "Immutable chronological audit log of all stage transitions",
            "Loan officer stage update controls (KYC, Underwriting, Sanction)",
            "Mandatory rejection reason code capture on adverse decisions",
            "SBI Underwriting Risk Summary card with visual progress bar"
        ])
    ]

    for i, (title, color, bullets) in enumerate(features):
        col = i % 2
        row = i // 2
        x = Inches(0.8) + col * Inches(6.0)
        y = Inches(1.5) + row * Inches(2.75)
        
        add_card(s7, x, y, Inches(5.733), Inches(2.55))

        bar = s7.shapes.add_shape(MSO_SHAPE.RECTANGLE, x, y, Inches(5.733), Inches(0.08))
        bar.fill.solid()
        bar.fill.fore_color.rgb = color
        bar.line.fill.background()

        tb = s7.shapes.add_textbox(x + Inches(0.2), y + Inches(0.15), Inches(5.333), Inches(2.3))
        tf = tb.text_frame
        tf.word_wrap = True

        p = tf.paragraphs[0]
        p.text = title
        p.font.size = Pt(13)
        p.font.bold = True
        p.font.color.rgb = color

        for b in bullets:
            pb = tf.add_paragraph()
            pb.text = "• " + b
            pb.font.size = Pt(10.5)
            pb.font.color.rgb = DARK_TEXT
            pb.space_before = Pt(4)

    # ==========================================================
    # SLIDE 8: Automated Quality Gate (Selenium E2E Quality Gate)
    # ==========================================================
    s8 = prs.slides.add_slide(blank_layout)
    set_slide_background(s8, LIGHT_BG)
    add_header(s8, "Automated Quality Gate (Selenium WebDriver & JUnit 5)")

    # Left: The 6 User Journeys
    add_card(s8, Inches(0.8), Inches(1.5), Inches(6.6), Inches(5.4))
    lb = s8.shapes.add_textbox(Inches(1.0), Inches(1.7), Inches(6.2), Inches(5.0))
    ltf = lb.text_frame
    ltf.word_wrap = True

    p = ltf.paragraphs[0]
    p.text = "6 Automated End-to-End User Journeys (Headless Chrome)"
    p.font.size = Pt(14)
    p.font.bold = True
    p.font.color.rgb = ROYAL_BLUE

    journeys = [
        ("Journey 1 (Intake)", "Submits Rahul Sharma (₹45,000, Personal) -> Asserts LOAN-XXXX appears on Dashboard with SUBMITTED status."),
        ("Journey 2 (Search)", "Enters 'Rahul' in search input -> Asserts matching row is displayed -> Clicks Reset and asserts table refreshes."),
        ("Journey 3 (Timeline)", "Drills down into applicant details -> Advances stage to DOCUMENT_VERIFIED -> Asserts audit event log recorded."),
        ("Journey 4 (Rejection)", "Updates status to REJECTED with reason 'CIBIL Score Below 650' -> Asserts routing into /alerts exception queue."),
        ("Journey 5 / SBI A (Eligible)", "Submits Ananya Kapoor (CIBIL 820, FOIR ~13%) -> Asserts PRE_APPROVED tag -> Fast-tracks to UNDERWRITING."),
        ("Journey 6 / SBI B (Ineligible)", "Submits Vikram Singh (CIBIL 580 < 650) -> Asserts auto HIGH_RISK_EXCEPTION -> Asserts automatic routing to Alerts queue.")
    ]
    for j_title, j_desc in journeys:
        pj = ltf.add_paragraph()
        pj.text = "✓ " + j_title + ": " + j_desc
        pj.font.size = Pt(10)
        pj.font.color.rgb = DARK_TEXT
        pj.space_before = Pt(5)

    # Right: Failure Strategy & Quality Gate Architecture
    add_card(s8, Inches(7.6), Inches(1.5), Inches(4.933), Inches(5.4))
    rb = s8.shapes.add_textbox(Inches(7.8), Inches(1.7), Inches(4.533), Inches(5.0))
    rtf = rb.text_frame
    rtf.word_wrap = True

    rp = rtf.paragraphs[0]
    rp.text = "Quality Gate Architecture"
    rp.font.size = Pt(14)
    rp.font.bold = True
    rp.font.color.rgb = NAVY

    qg_points = [
        ("Zero-Defect Pipeline Enforcement", "If any Selenium browser assertion fails, the pipeline aborts immediately. Docker image build and deployment are blocked."),
        ("ScreenshotOnFailureExtension", "JUnit 5 TestWatcher extension captures high-resolution PNG screenshots upon failure into target/screenshots/ for post-mortem debugging."),
        ("Deliberate Defect Injection Test (Task 9)", "A simulated UI regression (changing page title) was committed to test the quality gate. Jenkins caught the defect, aborted deployment, and preserved production stability."),
        ("100% Verification Record", "All 6 journeys passed with 0 errors, validating complete UI lifecycle reliability.")
    ]
    for title, desc in qg_points:
        p1 = rtf.add_paragraph()
        p1.text = "• " + title
        p1.font.size = Pt(11)
        p1.font.bold = True
        p1.font.color.rgb = DANGER_RED if "Defect" in title else TEAL
        p1.space_before = Pt(8)

        p2 = rtf.add_paragraph()
        p2.text = desc
        p2.font.size = Pt(9.5)
        p2.font.color.rgb = MUTED_TEXT

    # ==========================================================
    # SLIDE 9: CI/CD Pipeline as Code (Jenkinsfile)
    # ==========================================================
    s9 = prs.slides.add_slide(blank_layout)
    set_slide_background(s9, LIGHT_BG)
    add_header(s9, "Declarative CI/CD Pipeline as Code (Jenkinsfile)")

    pipeline_stages = [
        ("Stage 1: Checkout SCM", "Clones latest commit from GitHub repository main branch with clean working tree.", ROYAL_BLUE),
        ("Stage 2: Unit Tests", "Executes JUnit 5 service layer tests via Maven (12/12 passing with 100% coverage).", PURPLE),
        ("Stage 3: Selenium Quality Gate", "Launches headless Chrome browser to run 6 full-lifecycle E2E journeys.", DANGER_RED),
        ("Stage 4: Build Docker Image", "Builds multi-stage image tagged with loan-portal:build-<N> and loan-portal:latest.", TEAL),
        ("Stage 5: Continuous Deployment", "Gracefully stops running container and deploys newly built image mapped to Port 8081.", SUCCESS_GREEN),
        ("Stage 6: Actuator Health Check", "Automated smoke check curl probing http://localhost:8081/actuator/health for UP status.", GOLD),
        ("Stage 7: Post-Actions & Reports", "Archives JUnit XML reports, surefire test logs, and failure screenshots if triggered.", NAVY)
    ]

    for i, (title, desc, color) in enumerate(pipeline_stages):
        y = Inches(1.5) + i * Inches(0.75)
        # Stage bar
        add_card(s9, Inches(0.8), y, Inches(11.733), Inches(0.65))
        
        dot = s9.shapes.add_shape(MSO_SHAPE.OVAL, Inches(1.0), y + Inches(0.15), Inches(0.35), Inches(0.35))
        dot.fill.solid()
        dot.fill.fore_color.rgb = color
        dot.line.fill.background()

        tb = s9.shapes.add_textbox(Inches(1.5), y + Inches(0.05), Inches(10.8), Inches(0.55))
        tf = tb.text_frame
        tf.word_wrap = True
        
        p = tf.paragraphs[0]
        p.text = title + "  —  "
        p.font.size = Pt(12)
        p.font.bold = True
        p.font.color.rgb = NAVY
        
        run = p.add_run()
        run.text = desc
        run.font.size = Pt(11)
        run.font.bold = False
        run.font.color.rgb = DARK_TEXT

    # ==========================================================
    # SLIDE 10: Hardened Docker Containerization
    # ==========================================================
    s10 = prs.slides.add_slide(blank_layout)
    set_slide_background(s10, LIGHT_BG)
    add_header(s10, "Multi-Stage Docker Containerization & Security Hardening")

    # Left: Multi-stage Explanation
    add_card(s10, Inches(0.8), Inches(1.5), Inches(5.6), Inches(5.4))
    lb = s10.shapes.add_textbox(Inches(1.0), Inches(1.7), Inches(5.2), Inches(5.0))
    ltf = lb.text_frame
    ltf.word_wrap = True

    p = ltf.paragraphs[0]
    p.text = "Multi-Stage Build Pattern"
    p.font.size = Pt(16)
    p.font.bold = True
    p.font.color.rgb = ROYAL_BLUE

    ms_points = [
        "Stage 1 (Builder): Uses maven:3.9-eclipse-temurin-17 to compile code and package the Spring Boot executable JAR.",
        "Stage 2 (Runtime): Copies ONLY the compiled JAR into a lightweight eclipse-temurin:17-jre-alpine base image.",
        "Attack Surface Reduction: Build tools, Maven dependencies, source files, and compilers are excluded from the final image.",
        "Image Footprint Optimization: Final image size reduced by over 65% compared to monolithic container images.",
        "Fast Layer Caching: Dependencies separated from application code to accelerate CI/CD build execution."
    ]
    for pt in ms_points:
        p = ltf.add_paragraph()
        p.text = "• " + pt
        p.font.size = Pt(11)
        p.font.color.rgb = DARK_TEXT
        p.space_before = Pt(10)

    # Right: Security Hardening
    add_card(s10, Inches(6.8), Inches(1.5), Inches(5.733), Inches(5.4))
    rb = s10.shapes.add_textbox(Inches(7.0), Inches(1.7), Inches(5.333), Inches(5.0))
    rtf = rb.text_frame
    rtf.word_wrap = True

    rp = rtf.paragraphs[0]
    rp.text = "Production Security Hardening"
    rp.font.size = Pt(16)
    rp.font.bold = True
    rp.font.color.rgb = SUCCESS_GREEN

    sec_points = [
        ("Non-Root User Enforcement", "Creates system group appgroup and user appuser (UID 1001). Application runs with zero root privileges to prevent container breakout."),
        ("Native Container Healthcheck", "HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 CMD wget -qO- http://localhost:8081/actuator/health || exit 1"),
        ("Volume & Data Persistence", "Mounts host ./data to container /app/data so persistent local H2 database survives container destruction and recreation."),
        ("Deterministic Port Binding", "EXPOSE 8081 with runtime mapping -p 8081:8081 maintaining strict port isolation from Jenkins controller.")
    ]
    for st, sd in sec_points:
        p1 = rtf.add_paragraph()
        p1.text = "🛡️ " + st
        p1.font.size = Pt(11.5)
        p1.font.bold = True
        p1.font.color.rgb = NAVY
        p1.space_before = Pt(8)

        p2 = rtf.add_paragraph()
        p2.text = sd
        p2.font.size = Pt(10)
        p2.font.color.rgb = DARK_TEXT

    # ==========================================================
    # SLIDE 11: Configuration Management & Ansible Automation
    # ==========================================================
    s11 = prs.slides.add_slide(blank_layout)
    set_slide_background(s11, LIGHT_BG)
    add_header(s11, "Configuration Management & Ansible Idempotency Verification")

    # Left: Baseline
    add_card(s11, Inches(0.8), Inches(1.5), Inches(5.6), Inches(5.4))
    lb = s11.shapes.add_textbox(Inches(1.0), Inches(1.7), Inches(5.2), Inches(5.0))
    ltf = lb.text_frame
    ltf.word_wrap = True

    p = ltf.paragraphs[0]
    p.text = "Automated Server Baseline (IaC)"
    p.font.size = Pt(16)
    p.font.bold = True
    p.font.color.rgb = ROYAL_BLUE

    ans_items = [
        "Declarative YAML Playbook: Defines complete target environment state as code (playbook.yml).",
        "Dedicated Service User: Automatically provisions user loanapp (UID 1001) in group loanapp with docker privileges.",
        "Directory Hierarchy Enforced: /opt/loan-portal/ (0755), /logs/ (0755), /config/ (0750), and /data/ (0750).",
        "Environment Secret File: /opt/loan-portal/.env deployed with restrictive 0640 ownership.",
        "Firewall Baseline: UFW automatically allows Port 8081/tcp and Port 22/tcp while guarding controller ports."
    ]
    for it in ans_items:
        p = ltf.add_paragraph()
        p.text = "• " + it
        p.font.size = Pt(11)
        p.font.color.rgb = DARK_TEXT
        p.space_before = Pt(10)

    # Right: Idempotency Evidence
    add_card(s11, Inches(6.8), Inches(1.5), Inches(5.733), Inches(5.4))
    rb = s11.shapes.add_textbox(Inches(7.0), Inches(1.7), Inches(5.333), Inches(5.0))
    rtf = rb.text_frame
    rtf.word_wrap = True

    rp = rtf.paragraphs[0]
    rp.text = "Idempotency Proof (Run 1 vs. Run 2)"
    rp.font.size = Pt(16)
    rp.font.bold = True
    rp.font.color.rgb = SUCCESS_GREEN

    p_exp = rtf.add_paragraph()
    p_exp.text = "Idempotency guarantees that re-executing automation leaves the host in the exact desired state with zero duplicate processes or configuration drift."
    p_exp.font.size = Pt(10.5)
    p_exp.font.color.rgb = MUTED_TEXT
    p_exp.space_before = Pt(6)

    # Comparison Table
    table_data = [
        ("Metric", "Run 1 (Initial Setup)", "Run 2 (Audit Run)"),
        ("ok tasks", "3 tasks", "8 tasks"),
        ("changed tasks", "5 tasks (Created)", "0 tasks (ZERO drift)"),
        ("failed tasks", "0 failures", "0 failures"),
        ("Verdict", "Baseline Established", "PASSED: 100% Idempotent")
    ]
    for r1, r2, r3 in table_data:
        p = rtf.add_paragraph()
        p.text = f"{r1:18} | {r2:22} | {r3}"
        p.font.size = Pt(10)
        p.font.bold = (r1 == "Metric" or r1 == "Verdict")
        p.font.color.rgb = SUCCESS_GREEN if "PASSED" in r3 else DARK_TEXT
        p.space_before = Pt(6)

    # ==========================================================
    # SLIDE 12: High Availability, Observability & Rollback
    # ==========================================================
    s12 = prs.slides.add_slide(blank_layout)
    set_slide_background(s12, LIGHT_BG)
    add_header(s12, "Observability, Health Telemetry & Automated Rollback")

    # Left: Actuator Health Telemetry
    add_card(s12, Inches(0.8), Inches(1.5), Inches(5.6), Inches(5.4))
    lb = s12.shapes.add_textbox(Inches(1.0), Inches(1.7), Inches(5.2), Inches(5.0))
    ltf = lb.text_frame
    ltf.word_wrap = True

    p = ltf.paragraphs[0]
    p.text = "Spring Boot Actuator Telemetry"
    p.font.size = Pt(16)
    p.font.bold = True
    p.font.color.rgb = ROYAL_BLUE

    act_points = [
        "Endpoint: http://localhost:8081/actuator/health returns HTTP 200 OK with detailed subsystem status.",
        "Composite Probes: Evaluates relational database connection, disk space utilization, and ping response.",
        "JSON Telemetry Response:",
        "   {\n     \"status\": \"UP\",\n     \"components\": {\n       \"db\": { \"status\": \"UP\" },\n       \"diskSpace\": { \"status\": \"UP\" }\n     }\n   }",
        "Continuous Smoke Testing: Actuator endpoint is curled in Jenkins Stage 6 and verified by Docker healthcheck every 30 seconds."
    ]
    for ap in act_points:
        p = ltf.add_paragraph()
        p.text = ap
        p.font.size = Pt(10.5)
        p.font.color.rgb = DARK_TEXT
        p.space_before = Pt(8)

    # Right: Disaster Recovery Rollback
    add_card(s12, Inches(6.8), Inches(1.5), Inches(5.733), Inches(5.4))
    rb = s12.shapes.add_textbox(Inches(7.0), Inches(1.7), Inches(5.333), Inches(5.0))
    rtf = rb.text_frame
    rtf.word_wrap = True

    rp = rtf.paragraphs[0]
    rp.text = "Sub-15 Second Automated Disaster Recovery"
    rp.font.size = Pt(16)
    rp.font.bold = True
    rp.font.color.rgb = DANGER_RED

    rb_points = [
        ("Simulated Outage Detection", "Health probe continuously polls /actuator/health. If 3 consecutive failures occur, disaster recovery triggers."),
        ("Automated Rollback Script", "verify_reliability.ps1 automatically intercepts failure, stops defective container, and pulls verified release v1.0.0."),
        ("Sub-15 Second MTTR", "Container replacement and database reconnect complete in < 15 seconds, exceeding industry DORA targets."),
        ("Data Persistence Guarantee", "Because data is stored in the host ./data volume, ZERO loan application data is lost during disaster rollback.")
    ]
    for rt, rd in rb_points:
        p1 = rtf.add_paragraph()
        p1.text = "⚡ " + rt
        p1.font.size = Pt(11.5)
        p1.font.bold = True
        p1.font.color.rgb = NAVY
        p1.space_before = Pt(8)

        p2 = rtf.add_paragraph()
        p2.text = rd
        p2.font.size = Pt(10)
        p2.font.color.rgb = DARK_TEXT

    # ==========================================================
    # SLIDE 13: Phase 2 SBI Underwriting Domain Integration
    # ==========================================================
    s13 = prs.slides.add_slide(blank_layout)
    set_slide_background(s13, LIGHT_BG)
    add_header(s13, "Phase 2: State Bank of India (SBI) Domain Integration")

    sbi_pillars = [
        ("Pillar 1: CIBIL Credit Score", ROYAL_BLUE, [
            "Score >= 750: Preferential interest rate, auto PRE_APPROVED status.",
            "Score 650-749: Requires MANUAL_REVIEW by credit analyst.",
            "Score < 650: Automatic HIGH_RISK_EXCEPTION rejection."
        ]),
        ("Pillar 2: Income & Employment", TEAL, [
            "Salaried: Minimum net monthly salary of ₹20,000–₹25,000.",
            "Self-Employed: Business operational and profitable for >= 3 years.",
            "Income < ₹20,000 flagged as High Risk."
        ]),
        ("Pillar 3: Age Eligibility", PURPLE, [
            "Applicant age must be between 21 and 60 years at submission.",
            "Age < 21 or > 60 automatically routes to Exception queue.",
            "Validated dynamically on loan intake."
        ]),
        ("Pillar 4: FOIR Debt-to-Income", SUCCESS_GREEN, [
            "FOIR = (Existing EMIs + Proposed EMI) / Net Income * 100",
            "Threshold: Total obligations must be <= 50% of monthly income.",
            "FOIR > 50% automatically rejected / flagged."
        ])
    ]

    for i, (title, color, bullets) in enumerate(sbi_pillars):
        col = i % 2
        row = i // 2
        x = Inches(0.8) + col * Inches(6.0)
        y = Inches(1.5) + row * Inches(2.75)
        
        add_card(s13, x, y, Inches(5.733), Inches(2.55))

        bar = s13.shapes.add_shape(MSO_SHAPE.RECTANGLE, x, y, Inches(5.733), Inches(0.08))
        bar.fill.solid()
        bar.fill.fore_color.rgb = color
        bar.line.fill.background()

        tb = s13.shapes.add_textbox(x + Inches(0.2), y + Inches(0.15), Inches(5.333), Inches(2.3))
        tf = tb.text_frame
        tf.word_wrap = True

        p = tf.paragraphs[0]
        p.text = title
        p.font.size = Pt(13)
        p.font.bold = True
        p.font.color.rgb = color

        for b in bullets:
            pb = tf.add_paragraph()
            pb.text = "• " + b
            pb.font.size = Pt(10.5)
            pb.font.color.rgb = DARK_TEXT
            pb.space_before = Pt(4)

    # ==========================================================
    # SLIDE 14: Persistent Local Database Architecture
    # ==========================================================
    s14 = prs.slides.add_slide(blank_layout)
    set_slide_background(s14, LIGHT_BG)
    add_header(s14, "Database Architecture: In-Memory to Persistent Local Storage")

    # Left: Before vs After
    add_card(s14, Inches(0.8), Inches(1.5), Inches(5.6), Inches(5.4))
    lb = s14.shapes.add_textbox(Inches(1.0), Inches(1.7), Inches(5.2), Inches(5.0))
    ltf = lb.text_frame
    ltf.word_wrap = True

    p = ltf.paragraphs[0]
    p.text = "Storage Modernization (In-Memory -> File-Based)"
    p.font.size = Pt(15)
    p.font.bold = True
    p.font.color.rgb = ROYAL_BLUE

    db_points = [
        ("Previous State: In-Memory H2", "jdbc:h2:mem:loandb — Data existed only in volatile RAM. Every container restart or application stop resulted in 100% data loss.", DANGER_RED),
        ("Current State: Persistent Local H2", "jdbc:h2:file:./data/loandb;DB_CLOSE_DELAY=-1 — Data is saved locally to disk in the data/ folder (data/loandb.mv.db).", SUCCESS_GREEN),
        ("Durability Across Restarts", "Borrower applications, timestamped audit events, and KYC document records persist permanently across application and container restarts.", NAVY),
        ("H2 Web Console Access", "Available at http://localhost:8081/h2-console with user 'sa' for live SQL queries and schema inspection.", TEAL)
    ]
    for title, desc, col in db_points:
        p1 = ltf.add_paragraph()
        p1.text = "• " + title
        p1.font.size = Pt(11)
        p1.font.bold = True
        p1.font.color.rgb = col
        p1.space_before = Pt(8)

        p2 = ltf.add_paragraph()
        p2.text = desc
        p2.font.size = Pt(10)
        p2.font.color.rgb = DARK_TEXT

    # Right: KYC Document Vault & Data Model
    add_card(s14, Inches(6.8), Inches(1.5), Inches(5.733), Inches(5.4))
    rb = s14.shapes.add_textbox(Inches(7.0), Inches(1.7), Inches(5.333), Inches(5.0))
    rtf = rb.text_frame
    rtf.word_wrap = True

    rp = rtf.paragraphs[0]
    rp.text = "KYC Document Entity & Underwriting Schema"
    rp.font.size = Pt(15)
    rp.font.bold = True
    rp.font.color.rgb = NAVY

    kyc_items = [
        ("LoanDocument Entity", "Stores KYC files (PAN, Aadhaar, Bank Statement, Salary Slips, Form 16) with PENDING, VERIFIED, or REJECTED review status."),
        ("Secure Storage Outside Web Root", "Uploaded files stored at ./uploads/kyc-documents/ with UUID-based keys to prevent path traversal attacks."),
        ("New Underwriting Model Fields", "cibilScore, monthlyIncome, existingEmi, employmentType, age, panNumber, tenureMonths, sbiEligibilityTag, calculatedFoir."),
        ("Audit Events & Relational Mapping", "OneToMany cascade between LoanApplication and LoanEvent / LoanDocument ensuring atomic database transactions.")
    ]
    for kt, kd in kyc_items:
        p1 = rtf.add_paragraph()
        p1.text = "📂 " + kt
        p1.font.size = Pt(11.5)
        p1.font.bold = True
        p1.font.color.rgb = ROYAL_BLUE
        p1.space_before = Pt(8)

        p2 = rtf.add_paragraph()
        p2.text = kd
        p2.font.size = Pt(10)
        p2.font.color.rgb = DARK_TEXT

    # ==========================================================
    # SLIDE 15: Industry DORA Metrics & Performance Benchmarks
    # ==========================================================
    s15 = prs.slides.add_slide(blank_layout)
    set_slide_background(s15, LIGHT_BG)
    add_header(s15, "Industry DORA Metrics & DevOps Performance Benchmarks")

    dora_metrics = [
        ("Deployment Frequency", "High / Daily", "On-Demand Automated", "Every Git commit to main triggers automatic pipeline build, test, and container deployment.", ROYAL_BLUE),
        ("Lead Time for Changes", "< 1 Day", "< 5 Minutes", "From developer 'git push' to running Docker container passing health check on Port 8081.", SUCCESS_GREEN),
        ("Change Failure Rate", "< 15%", "0% in Production", "Defects, regressions, and UI errors caught early by the Selenium headless browser quality gate.", TEAL),
        ("Mean Time to Recovery (MTTR)", "< 1 Hour", "< 15 Seconds", "Automated rollback script swaps failing container back to previous stable release v1.0.0.", DANGER_RED)
    ]

    card_w = Inches(2.75)
    gap = Inches(0.24)
    start_x = Inches(0.8)

    for i, (m_title, industry, achieved, desc, color) in enumerate(dora_metrics):
        x = start_x + i * (card_w + gap)
        add_card(s15, x, Inches(1.5), card_w, Inches(5.4))
        
        bar = s15.shapes.add_shape(MSO_SHAPE.RECTANGLE, x, Inches(1.5), card_w, Inches(0.12))
        bar.fill.solid()
        bar.fill.fore_color.rgb = color
        bar.line.fill.background()

        tb = s15.shapes.add_textbox(x + Inches(0.15), Inches(1.7), card_w - Inches(0.3), Inches(5.0))
        tf = tb.text_frame
        tf.word_wrap = True

        p1 = tf.paragraphs[0]
        p1.text = m_title
        p1.font.size = Pt(13)
        p1.font.bold = True
        p1.font.color.rgb = NAVY

        # Big metric number
        p_num = tf.add_paragraph()
        p_num.text = achieved
        p_num.font.size = Pt(22)
        p_num.font.bold = True
        p_num.font.color.rgb = color
        p_num.space_before = Pt(8)

        p_ind = tf.add_paragraph()
        p_ind.text = f"Industry Standard: {industry}"
        p_ind.font.size = Pt(10)
        p_ind.font.bold = True
        p_ind.font.color.rgb = MUTED_TEXT
        p_ind.space_before = Pt(4)

        p_desc = tf.add_paragraph()
        p_desc.text = desc
        p_desc.font.size = Pt(10.5)
        p_desc.font.color.rgb = DARK_TEXT
        p_desc.space_before = Pt(12)

    # ==========================================================
    # SLIDE 16: Viva Defense & Technical Examination Highlights
    # ==========================================================
    s16 = prs.slides.add_slide(blank_layout)
    set_slide_background(s16, LIGHT_BG)
    add_header(s16, "Examiner Viva Defense & Key Technical Justifications")

    qa_list = [
        ("Q: Why Declarative Pipeline (Jenkinsfile) over Freestyle Jobs?",
         "A: Jenkinsfile represents Pipeline as Code. Stored directly in Git, it provides versioning, code reviews, auditability, and instant disaster recovery compared to opaque UI clicks."),
        ("Q: How does the Selenium Quality Gate protect production?",
         "A: It executes real user journeys in headless Chrome prior to Docker image creation. If any assertion fails, deployment aborts immediately, preventing broken code from reaching users."),
        ("Q: What is Idempotency in Ansible and why is it critical?",
         "A: Idempotency guarantees executing a playbook multiple times produces the exact target state with zero duplicate changes. Proven in Task 13: Run 2 reported changed=0, failed=0."),
        ("Q: Why enforce strict Port 8080 and 8081 separation?",
         "A: Prevents port contention. Port 8080 is dedicated exclusively to Jenkins CI/CD controller; Port 8081 is assigned exclusively to the Loan Portal application.")
    ]

    for i, (q, a) in enumerate(qa_list):
        y = Inches(1.5) + i * Inches(1.3)
        add_card(s16, Inches(0.8), y, Inches(11.733), Inches(1.18))
        
        tb = s16.shapes.add_textbox(Inches(1.0), y + Inches(0.08), Inches(11.333), Inches(1.0))
        tf = tb.text_frame
        tf.word_wrap = True

        pq = tf.paragraphs[0]
        pq.text = q
        pq.font.size = Pt(12)
        pq.font.bold = True
        pq.font.color.rgb = ROYAL_BLUE

        pa = tf.add_paragraph()
        pa.text = a
        pa.font.size = Pt(10.5)
        pa.font.color.rgb = DARK_TEXT
        pa.space_before = Pt(4)

    # ==========================================================
    # SLIDE 17: Conclusion & Future Scope (Dark Theme)
    # ==========================================================
    s17 = prs.slides.add_slide(blank_layout)
    set_slide_background(s17, NAVY)

    # Top accent bar
    top_bar17 = s17.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0), Inches(0), Inches(13.333), Inches(0.15))
    top_bar17.fill.solid()
    top_bar17.fill.fore_color.rgb = TEAL
    top_bar17.line.fill.background()

    # Title
    t17 = s17.shapes.add_textbox(Inches(1.0), Inches(0.8), Inches(11.333), Inches(0.8))
    tf17 = t17.text_frame
    tf17.word_wrap = True
    p = tf17.paragraphs[0]
    p.text = "Project Summary, Future Scope & Academic Sign-off"
    p.font.size = Pt(28)
    p.font.bold = True
    p.font.color.rgb = WHITE

    # Left: Summary Card
    add_card(s17, Inches(1.0), Inches(1.8), Inches(5.4), Inches(4.0), bg_color=RGBColor(23, 42, 69), border_color=RGBColor(51, 65, 85))
    sb1 = s17.shapes.add_textbox(Inches(1.2), Inches(1.95), Inches(5.0), Inches(3.7))
    stf1 = sb1.text_frame
    stf1.word_wrap = True

    sp1 = stf1.paragraphs[0]
    sp1.text = "Summary of Completed Work"
    sp1.font.size = Pt(16)
    sp1.font.bold = True
    sp1.font.color.rgb = GOLD

    s_items = [
        "15/15 DevOps tasks completed with individual documentation.",
        "100% test pass rate (19 tests: 13 unit/service + 6 Selenium E2E).",
        "Persistent local database storing records at ./data/loandb.mv.db.",
        "SBI Retail Underwriting Engine integrated (CIBIL, FOIR, Age, Income).",
        "Hardened non-root multi-stage Docker containerization on Port 8081.",
        "Ansible automated provisioning with verified 100% idempotency."
    ]
    for si in s_items:
        p = stf1.add_paragraph()
        p.text = "✓ " + si
        p.font.size = Pt(11)
        p.font.color.rgb = WHITE
        p.space_before = Pt(6)

    # Right: Future Scope & Q&A
    add_card(s17, Inches(6.8), Inches(1.8), Inches(5.533), Inches(4.0), bg_color=RGBColor(23, 42, 69), border_color=RGBColor(51, 65, 85))
    sb2 = s17.shapes.add_textbox(Inches(7.0), Inches(1.95), Inches(5.133), Inches(3.7))
    stf2 = sb2.text_frame
    stf2.word_wrap = True

    sp2 = stf2.paragraphs[0]
    sp2.text = "Phase 3 Future Enhancements & Q&A"
    sp2.font.size = Pt(16)
    sp2.font.bold = True
    sp2.font.color.rgb = TEAL

    f_items = [
        "Live Bureau Integration: Direct REST connectivity to CIBIL / Experian APIs.",
        "Automated Sanction Letters: Dynamic PDF generator upon approval.",
        "Kubernetes Orchestration: Helm charts, Ingress, & horizontal autoscaling.",
        "Role-Based Access Control: Spring Security RBAC with JWT / OAuth2."
    ]
    for fi in f_items:
        p = stf2.add_paragraph()
        p.text = "🚀 " + fi
        p.font.size = Pt(11)
        p.font.color.rgb = WHITE
        p.space_before = Pt(6)

    # Viva Signoff Banner
    v_box = s17.shapes.add_textbox(Inches(1.0), Inches(6.0), Inches(11.333), Inches(0.9))
    vtf = v_box.text_frame
    vtf.word_wrap = True
    vp1 = vtf.paragraphs[0]
    vp1.text = "Thank You!  |  Candidate: Vicky Vilas Pukale (23102A0033)  |  Open for Examiner Viva Questions"
    vp1.alignment = PP_ALIGN.CENTER
    vp1.font.size = Pt(15)
    vp1.font.bold = True
    vp1.font.color.rgb = GOLD

    output_path = r"d:\Vicky_Project\DevOps\DevOps_Project - Copy\DevOps_Project_Vicky_Pukale_Presentation.pptx"
    prs.save(output_path)
    print(f"Presentation saved successfully to {output_path}")

if __name__ == "__main__":
    create_deck()
