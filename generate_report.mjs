import {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
  HeadingLevel, AlignmentType, PageNumber, NumberFormat, Header, Footer,
  WidthType, BorderStyle, ShadingType, PageOrientation, convertInchesToTwip,
  TableOfContents, StyleLevel, LevelFormat, UnderlineType
} from "docx";
import { writeFileSync } from "fs";

const SCREENS = [
  {
    name: "SplashActivity",
    purpose: "App entry point that checks Firebase authentication state and routes the user accordingly.",
    navigatesTo: "LoginActivity (not logged in), MainActivity (logged in)",
    navigatesFrom: "System launcher",
    parameters: "None",
  },
  {
    name: "LoginActivity",
    purpose: "Handles email/password login and Google Sign-In via Firebase Authentication.",
    navigatesTo: "MainActivity (on success), SignupActivity",
    navigatesFrom: "SplashActivity",
    parameters: "Passed out: uid, username",
  },
  {
    name: "SignupActivity",
    purpose: "New user registration with email and password via Firebase Authentication.",
    navigatesTo: "OnboardingActivity (on success)",
    navigatesFrom: "LoginActivity",
    parameters: "Passed out: uid",
  },
  {
    name: "OnboardingActivity",
    purpose: "First-time user onboarding flow presented as a multi-step ViewPager2 carousel.",
    navigatesTo: "ProfileSetup1Activity",
    navigatesFrom: "SignupActivity",
    parameters: "Received: uid",
  },
  {
    name: "MainActivity",
    purpose: "Central dashboard hub displaying step, water, and sleep summary cards with a navigation drawer.",
    navigatesTo: "StepCounterActivity, WaterIntakeActivity, RecordSleepActivity, ShopActivity",
    navigatesFrom: "LoginActivity, SplashActivity",
    parameters: "Passed: uid",
  },
  {
    name: "StepCounterActivity",
    purpose: "Step tracking dashboard showing daily steps, distance, calories, and a weekly bar chart.",
    navigatesTo: "MainActivity (back)",
    navigatesFrom: "MainActivity",
    parameters: "Received: uid",
  },
  {
    name: "WaterIntakeActivity",
    purpose: "Allows the user to log daily water consumption and view intake progress.",
    navigatesTo: "MainActivity (back), WaterHistoryActivity",
    navigatesFrom: "MainActivity",
    parameters: "Received: uid",
  },
  {
    name: "RecordSleepActivity",
    purpose: "Enables the user to log nightly sleep duration and quality rating.",
    navigatesTo: "MainActivity (back), SleepHistoryActivity",
    navigatesFrom: "MainActivity",
    parameters: "Received: uid",
  },
  {
    name: "PreRunActivity",
    purpose: "Pre-run setup screen for configuring goal distance and warm-up preferences.",
    navigatesTo: "RunTrackingActivity",
    navigatesFrom: "MainActivity / HomeFragment",
    parameters: "Received: uid",
  },
  {
    name: "RunTrackingActivity",
    purpose: "Real-time GPS run tracking screen powered by a foreground service.",
    navigatesTo: "RunSummaryActivity",
    navigatesFrom: "PreRunActivity",
    parameters: "Received: uid",
  },
  {
    name: "RunSummaryActivity",
    purpose: "Displays post-run statistics and awards XP and coins based on performance.",
    navigatesTo: "MainActivity, RunHistoryActivity",
    navigatesFrom: "RunTrackingActivity",
    parameters: "Received: uid, distance, duration, coinsEarned, xpEarned",
  },
  {
    name: "RunHistoryActivity",
    purpose: "Browsable list of all past run sessions stored in Firestore.",
    navigatesTo: "RunDetailActivity",
    navigatesFrom: "RunSummaryActivity, MainActivity",
    parameters: "Received: uid",
  },
  {
    name: "GamificationActivity",
    purpose: "Gamification hub showing XP, coins, and gems overview with links to sub-screens.",
    navigatesTo: "BadgeSystemActivity, DailyRewardActivity, QuestBoardActivity, ShopActivity",
    navigatesFrom: "HomeActivity bottom navigation",
    parameters: "Received: uid",
  },
  {
    name: "BadgeSystemActivity",
    purpose: "Displays the user's badge collection, rarity tiers, and unlock progress.",
    navigatesTo: "GamificationActivity (back)",
    navigatesFrom: "GamificationActivity",
    parameters: "Received: uid",
  },
  {
    name: "DailyRewardActivity",
    purpose: "Allows the user to claim a once-per-day randomized XP and coin reward.",
    navigatesTo: "GamificationActivity (back)",
    navigatesFrom: "GamificationActivity",
    parameters: "Received: uid",
  },
  {
    name: "ShopActivity",
    purpose: "In-app shop where users spend coins on items and streak shields.",
    navigatesTo: "MainActivity (back)",
    navigatesFrom: "MainActivity drawer, GamificationActivity",
    parameters: "Received: uid",
  },
  {
    name: "StreakDashboardFragment",
    purpose: "Shows a 28-day activity calendar grid, current streak, longest streak, and shield inventory.",
    navigatesTo: "N/A (fragment hosted in HomeActivity)",
    navigatesFrom: "HomeActivity bottom navigation",
    parameters: "Received: uid",
  },
  {
    name: "LeaderboardFragment",
    purpose: "Displays a global or friends-based XP ranking leaderboard.",
    navigatesTo: "N/A (fragment hosted in HomeActivity)",
    navigatesFrom: "HomeActivity bottom navigation",
    parameters: "Received: uid",
  },
  {
    name: "ProfileFragment",
    purpose: "Shows the user's profile, aggregated stats, and provides access to settings.",
    navigatesTo: "SettingsActivity",
    navigatesFrom: "HomeActivity bottom navigation",
    parameters: "Received: uid",
  },
  {
    name: "SettingsActivity",
    purpose: "Notification preferences and account management settings screen.",
    navigatesTo: "ProfileFragment (back)",
    navigatesFrom: "ProfileFragment",
    parameters: "Received: uid",
  },
];

// ── Helpers ──────────────────────────────────────────────────────────────────

function heading1(text) {
  return new Paragraph({
    text,
    heading: HeadingLevel.HEADING_1,
    spacing: { before: 400, after: 200 },
  });
}

function heading2(text) {
  return new Paragraph({
    text,
    heading: HeadingLevel.HEADING_2,
    spacing: { before: 300, after: 120 },
  });
}

function heading3(text) {
  return new Paragraph({
    text,
    heading: HeadingLevel.HEADING_3,
    spacing: { before: 200, after: 80 },
  });
}

function body(text) {
  return new Paragraph({
    children: [new TextRun({ text, font: "Calibri", size: 22 })],
    spacing: { after: 120 },
  });
}

function labelValue(label, value) {
  return new Paragraph({
    children: [
      new TextRun({ text: `${label}: `, bold: true, font: "Calibri", size: 22 }),
      new TextRun({ text: value, font: "Calibri", size: 22 }),
    ],
    spacing: { after: 80 },
  });
}

function spacer() {
  return new Paragraph({ text: "", spacing: { after: 100 } });
}

function pageBreak() {
  return new Paragraph({
    children: [new TextRun({ break: 1 })],
  });
}

// ── Table helpers ─────────────────────────────────────────────────────────────

const HEADER_SHADING = { fill: "1F3864", type: ShadingType.CLEAR, color: "auto" };
const ROW_SHADING_ALT = { fill: "DCE6F1", type: ShadingType.CLEAR, color: "auto" };

function headerCell(text) {
  return new TableCell({
    shading: HEADER_SHADING,
    children: [
      new Paragraph({
        children: [new TextRun({ text, bold: true, color: "FFFFFF", font: "Calibri", size: 20 })],
        alignment: AlignmentType.CENTER,
        spacing: { before: 60, after: 60 },
      }),
    ],
  });
}

function dataCell(text, shade = false) {
  return new TableCell({
    shading: shade ? ROW_SHADING_ALT : undefined,
    children: [
      new Paragraph({
        children: [new TextRun({ text, font: "Calibri", size: 20 })],
        spacing: { before: 60, after: 60 },
      }),
    ],
  });
}

// ── Cover Page ────────────────────────────────────────────────────────────────

function buildCoverPage() {
  return [
    new Paragraph({ text: "", spacing: { after: 2000 } }),
    new Paragraph({
      children: [
        new TextRun({
          text: "FIDA Android Application",
          bold: true,
          font: "Calibri",
          size: 56,
          color: "1F3864",
        }),
      ],
      alignment: AlignmentType.CENTER,
      spacing: { after: 200 },
    }),
    new Paragraph({
      children: [
        new TextRun({
          text: "Screen Navigation Report",
          bold: true,
          font: "Calibri",
          size: 48,
          color: "2E74B5",
        }),
      ],
      alignment: AlignmentType.CENTER,
      spacing: { after: 400 },
    }),
    new Paragraph({
      children: [new TextRun({ text: "─".repeat(40), font: "Calibri", size: 24, color: "AAAAAA" })],
      alignment: AlignmentType.CENTER,
      spacing: { after: 400 },
    }),
    new Paragraph({
      children: [
        new TextRun({ text: "School Project Report", font: "Calibri", size: 32, italics: true, color: "555555" }),
      ],
      alignment: AlignmentType.CENTER,
      spacing: { after: 200 },
    }),
    new Paragraph({
      children: [new TextRun({ text: "May 2026", font: "Calibri", size: 28, color: "555555" })],
      alignment: AlignmentType.CENTER,
      spacing: { after: 200 },
    }),
    new Paragraph({
      children: [new TextRun({ text: "FIDA Development Team", font: "Calibri", size: 24, color: "777777" })],
      alignment: AlignmentType.CENTER,
      spacing: { after: 200 },
    }),
    pageBreak(),
  ];
}

// ── Table of Contents ─────────────────────────────────────────────────────────

function buildTOC() {
  const entries = [
    ["1.", "Introduction", "3"],
    ["1.1", "Project Overview", "3"],
    ["1.2", "Navigation Architecture", "3"],
    ["1.3", "Navigation Model Types", "4"],
    ["2.", "Screen-by-Screen Navigation", "5"],
    ...SCREENS.map((s, i) => [`2.${i + 1}`, s.name, String(5 + i)]),
    ["3.", "Navigation Flow Summary Table", String(5 + SCREENS.length)],
  ];

  const rows = entries.map(([num, title, page]) =>
    new Paragraph({
      children: [
        new TextRun({ text: `${num}  ${title}`, font: "Calibri", size: 22 }),
        new TextRun({ text: `\t${page}`, font: "Calibri", size: 22 }),
      ],
      tabStops: [{ type: "right", position: convertInchesToTwip(6) }],
      spacing: { after: 80 },
      indent: num.includes(".") && num !== "1." && num !== "2." && num !== "3."
        ? { left: 360 }
        : undefined,
    })
  );

  return [
    heading1("Table of Contents"),
    ...rows,
    pageBreak(),
  ];
}

// ── Section 1: Introduction ───────────────────────────────────────────────────

function buildIntroduction() {
  return [
    heading1("1. Introduction"),
    heading2("1.1 Project Overview"),
    body(
      "FIDA is a gamified fitness and wellness Android application designed to motivate users toward healthier habits " +
      "through game mechanics. The app combines health tracking features — including GPS run tracking, step counting, " +
      "water intake logging, and sleep recording — with a gamification economy of XP, coins, and gems. Users earn " +
      "rewards for completing fitness activities, unlock badges, maintain activity streaks, and compete on a global leaderboard."
    ),
    body(
      "This report documents the complete screen-to-screen navigation of the FIDA application, covering all 20 screens " +
      "and fragments. For each screen, the report identifies its purpose, the screens it navigates to and from, and the " +
      "parameters passed between screens via Android Intent extras."
    ),
    heading2("1.2 Navigation Architecture"),
    body(
      "The FIDA app uses a hybrid navigation model that combines standalone Activities with Fragment-based navigation " +
      "hosted inside a central HomeActivity. The architecture is structured as follows:"
    ),
    new Paragraph({
      children: [new TextRun({ text: "Activity-based navigation:", bold: true, font: "Calibri", size: 22 }), new TextRun({ text: " Feature screens such as StepCounterActivity, WaterIntakeActivity, RecordSleepActivity, and all run-tracking screens are standalone Activities launched via explicit Intents.", font: "Calibri", size: 22 })],
      spacing: { after: 100 },
      bullet: { level: 0 },
    }),
    new Paragraph({
      children: [new TextRun({ text: "Fragment-based navigation:", bold: true, font: "Calibri", size: 22 }), new TextRun({ text: " HomeActivity hosts a BottomNavigationView that swaps between four fragments: Home, StreakDashboard, Leaderboard, and Profile. Fragment transactions are managed manually via supportFragmentManager.", font: "Calibri", size: 22 })],
      spacing: { after: 100 },
      bullet: { level: 0 },
    }),
    new Paragraph({
      children: [new TextRun({ text: "Authentication flow:", bold: true, font: "Calibri", size: 22 }), new TextRun({ text: " SplashActivity acts as the entry point and routes users to LoginActivity or MainActivity based on Firebase Authentication state.", font: "Calibri", size: 22 })],
      spacing: { after: 100 },
      bullet: { level: 0 },
    }),
    new Paragraph({
      children: [new TextRun({ text: "Parameter passing:", bold: true, font: "Calibri", size: 22 }), new TextRun({ text: " All screens receive the Firebase user ID (uid) as an Intent extra, ensuring each screen can query the correct user's data from Firestore.", font: "Calibri", size: 22 })],
      spacing: { after: 200 },
      bullet: { level: 0 },
    }),
    heading2("1.3 Navigation Model Types"),
    body("The app employs three distinct navigation patterns:"),
    new Table({
      width: { size: 100, type: WidthType.PERCENTAGE },
      rows: [
        new TableRow({
          children: [
            headerCell("Pattern"),
            headerCell("Description"),
            headerCell("Examples"),
          ],
          tableHeader: true,
        }),
        new TableRow({
          children: [
            dataCell("Linear / Sequential"),
            dataCell("One screen leads to the next in a fixed order"),
            dataCell("Splash → Login → Main"),
          ],
        }),
        new TableRow({
          children: [
            dataCell("Hub & Spoke", true),
            dataCell("A central screen launches multiple feature screens", true),
            dataCell("MainActivity → Feature screens", true),
          ],
        }),
        new TableRow({
          children: [
            dataCell("Bottom Navigation"),
            dataCell("Fragments swapped within a host Activity"),
            dataCell("HomeActivity fragments"),
          ],
        }),
      ],
    }),
    spacer(),
    pageBreak(),
  ];
}

// ── Section 2: Screen-by-Screen ───────────────────────────────────────────────

function buildScreenSections() {
  const paragraphs = [heading1("2. Screen-by-Screen Navigation")];

  SCREENS.forEach((screen, i) => {
    paragraphs.push(heading2(`2.${i + 1}  ${screen.name}`));
    paragraphs.push(labelValue("Purpose", screen.purpose));
    paragraphs.push(labelValue("Navigates To", screen.navigatesTo));
    paragraphs.push(labelValue("Navigates From", screen.navigatesFrom));
    paragraphs.push(labelValue("Parameters", screen.parameters));
    paragraphs.push(spacer());
  });

  paragraphs.push(pageBreak());
  return paragraphs;
}

// ── Section 3: Summary Table ──────────────────────────────────────────────────

function buildSummaryTable() {
  const headerRow = new TableRow({
    children: [
      headerCell("Screen"),
      headerCell("Navigates To"),
      headerCell("Navigates From"),
      headerCell("Parameters"),
    ],
    tableHeader: true,
  });

  const dataRows = SCREENS.map((s, i) =>
    new TableRow({
      children: [
        dataCell(s.name, i % 2 !== 0),
        dataCell(s.navigatesTo, i % 2 !== 0),
        dataCell(s.navigatesFrom, i % 2 !== 0),
        dataCell(s.parameters, i % 2 !== 0),
      ],
    })
  );

  return [
    heading1("3. Navigation Flow Summary Table"),
    body(
      "The table below provides a consolidated reference of all 20 screens, their navigation targets, " +
      "their entry points, and the Intent parameters exchanged."
    ),
    spacer(),
    new Table({
      width: { size: 100, type: WidthType.PERCENTAGE },
      columnWidths: [2000, 2500, 2500, 2000],
      rows: [headerRow, ...dataRows],
    }),
    spacer(),
  ];
}

// ── Assemble Document ─────────────────────────────────────────────────────────

const doc = new Document({
  styles: {
    default: {
      document: {
        run: { font: "Calibri", size: 22 },
      },
    },
    paragraphStyles: [
      {
        id: "Heading1",
        name: "Heading 1",
        basedOn: "Normal",
        next: "Normal",
        quickFormat: true,
        run: { font: "Calibri", size: 32, bold: true, color: "1F3864" },
        paragraph: { spacing: { before: 400, after: 200 } },
      },
      {
        id: "Heading2",
        name: "Heading 2",
        basedOn: "Normal",
        next: "Normal",
        quickFormat: true,
        run: { font: "Calibri", size: 26, bold: true, color: "2E74B5" },
        paragraph: { spacing: { before: 300, after: 120 } },
      },
      {
        id: "Heading3",
        name: "Heading 3",
        basedOn: "Normal",
        next: "Normal",
        quickFormat: true,
        run: { font: "Calibri", size: 24, bold: true, color: "2E74B5" },
        paragraph: { spacing: { before: 200, after: 80 } },
      },
    ],
  },
  sections: [
    {
      properties: {
        page: {
          size: { width: convertInchesToTwip(8.27), height: convertInchesToTwip(11.69) },
          margin: {
            top: convertInchesToTwip(1),
            bottom: convertInchesToTwip(1),
            left: convertInchesToTwip(1.18),
            right: convertInchesToTwip(1.18),
          },
        },
      },
      headers: {
        default: new Header({
          children: [
            new Paragraph({
              children: [
                new TextRun({ text: "FIDA Android Application — Screen Navigation Report", font: "Calibri", size: 18, color: "888888" }),
              ],
              alignment: AlignmentType.RIGHT,
              border: { bottom: { style: BorderStyle.SINGLE, size: 6, color: "CCCCCC" } },
            }),
          ],
        }),
      },
      footers: {
        default: new Footer({
          children: [
            new Paragraph({
              children: [
                new TextRun({ text: "FIDA Development Team  |  School Project Report  |  May 2026     Page ", font: "Calibri", size: 18, color: "888888" }),
                new TextRun({ children: [PageNumber.CURRENT], font: "Calibri", size: 18, color: "888888" }),
                new TextRun({ text: " of ", font: "Calibri", size: 18, color: "888888" }),
                new TextRun({ children: [PageNumber.TOTAL_PAGES], font: "Calibri", size: 18, color: "888888" }),
              ],
              alignment: AlignmentType.CENTER,
              border: { top: { style: BorderStyle.SINGLE, size: 6, color: "CCCCCC" } },
            }),
          ],
        }),
      },
      children: [
        ...buildCoverPage(),
        ...buildTOC(),
        ...buildIntroduction(),
        ...buildScreenSections(),
        ...buildSummaryTable(),
      ],
    },
  ],
});

const buffer = await Packer.toBuffer(doc);
writeFileSync("D:/dafi/fida-new/fida-to-tranferv2/FIDA_Navigation_Report.docx", buffer);
console.log("Done: FIDA_Navigation_Report.docx created successfully.");
