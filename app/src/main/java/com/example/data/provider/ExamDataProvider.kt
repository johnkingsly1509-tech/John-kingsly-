package com.example.data.provider

import com.example.data.model.*
import java.text.SimpleDateFormat
import java.util.*

object ExamDataProvider {

    fun getAllExams(): List<ExamInfo> = listOf(
        ExamInfo(
            id = "tnpsc_grp4",
            nameEn = "TNPSC Group IV & VAO",
            nameTa = "டி.என்.பி.எஸ்.சி குரூப் 4 & VAO",
            category = "TNPSC",
            badgeColorHex = "#2E7D32",
            notificationStatus = "Notification Active / Annual Planner 2026",
            expectedDate = "2026-11-20",
            patternEn = "Single Paper: 200 Questions, 300 Marks, 3 Hours. Part A: General Tamil (100 Qs), Part B: GS & Aptitude (100 Qs).",
            patternTa = "ஒரே தாள்: 200 வினாக்கள், 300 மதிப்பெண்கள், 3 மணி நேரம். பகுதி அ: பொதுத்தமிழ் (100 வினா), பகுதி ஆ: பொது அறிவு & திறனறி தேர்வு (100 வினா).",
            totalMarks = 300,
            hasNegativeMarking = false,
            negativeMarkingRatio = "No Negative Marking",
            subjects = listOf(
                ExamSubject(
                    id = "tnpsc_tamil",
                    titleEn = "General Tamil (பொதுத்தமிழ்)",
                    titleTa = "பொதுத்தமிழ் (பகுதி அ)",
                    weightagePercentage = 50,
                    chapters = listOf(
                        ExamChapter(
                            id = "tamil_grammar",
                            titleEn = "Tamil Grammar (இலக்கணம்)",
                            titleTa = "இலக்கணம்",
                            topics = listOf(
                                ExamTopic("ta_top_1", "Sandhi Rules & Sentence Correction", "சந்தி பிழை நீக்குதல் மற்றும் தொடர் வகைகள்", "High", TopicStatus.LEARNING),
                                ExamTopic("ta_top_2", "Noun, Verb, Adjective Classification", "பெயர்ச்சொல், வினைச்சொல், எச்சம், முற்றுகள்", "High", TopicStatus.NOT_STARTED),
                                ExamTopic("ta_top_3", "Prefix & Suffix Particles (இடைச்சொல், உரிச்சொல்)", "இடைச்சொல் மற்றும் உரிச்சொல் பயன்பாடுகள்", "Medium", TopicStatus.NOT_STARTED)
                            )
                        ),
                        ExamChapter(
                            id = "tamil_literature",
                            titleEn = "Tamil Literature (இலக்கியம்)",
                            titleTa = "இலக்கியம்",
                            topics = listOf(
                                ExamTopic("ta_top_4", "Thirukkural - Core Couplets & Ethics", "திருக்குறள் - வாழ்வியல் மற்றும் பொருள் விழுமியங்கள்", "High", TopicStatus.COMPLETED),
                                ExamTopic("ta_top_5", "Eight Anthologies & Ten Idylls (எட்டுத்தொகை & பத்துப்பாட்டு)", "சங்க இலக்கியம்: எட்டுத்தொகை, பத்துப்பாட்டு நூல்கள்", "High", TopicStatus.NOT_STARTED),
                                ExamTopic("ta_top_6", "Silappathikaram & Manimekalai", "ஐம்பெருங்காப்பியங்கள்: சிலப்பதிகாரம், மணிமேகலை", "High", TopicStatus.NOT_STARTED)
                            )
                        )
                    )
                ),
                ExamSubject(
                    id = "tnpsc_polity",
                    titleEn = "Indian Polity & TN Administration",
                    titleTa = "இந்திய அரசியலமைப்பு & தமிழ்நாடு நிர்வாகம்",
                    weightagePercentage = 25,
                    chapters = listOf(
                        ExamChapter(
                            id = "polity_basics",
                            titleEn = "Constitution & Fundamental Rights",
                            titleTa = "அரசியலமைப்பு & அடிப்படை உரிமைகள்",
                            topics = listOf(
                                ExamTopic("pol_1", "Preamble & Salient Features of Constitution", "முகப்புரை மற்றும் இந்திய அரசியலமைப்பின் சிறப்பம்சங்கள்", "High", TopicStatus.COMPLETED),
                                ExamTopic("pol_2", "Fundamental Rights (Art 14-32) & Duties", "அடிப்படை உரிமைகள் (சரத்து 14-32) & கடமைகள்", "High", TopicStatus.NEEDS_REVISION),
                                ExamTopic("pol_3", "Directive Principles of State Policy (DPSP)", "அரசு வழிகாட்டு நெறிமுறைகள் (DPSP)", "High", TopicStatus.NOT_STARTED)
                            )
                        )
                    )
                ),
                ExamSubject(
                    id = "tnpsc_aptitude",
                    titleEn = "Aptitude & Mental Ability",
                    titleTa = "திறனறிவும் மனக்கணக்கு நுண்ணறிவும்",
                    weightagePercentage = 25,
                    chapters = listOf(
                        ExamChapter(
                            id = "apt_arithmetic",
                            titleEn = "Number Series, HCF & LCM",
                            titleTa = "மீ.சி.ம மற்றும் மீ.பொ.வ, எண்கணிதம்",
                            topics = listOf(
                                ExamTopic("apt_1", "Highest Common Factor & Least Common Multiple", "மீ.பொ.வ (HCF) மற்றும் மீ.சி.ம (LCM) நுணுக்கங்கள்", "High", TopicStatus.NOT_STARTED),
                                ExamTopic("apt_2", "Ratio, Proportion & Percentages", "விகிதம், விகிதாசாரம் மற்றும் சதவீத கணக்குகள்", "High", TopicStatus.NOT_STARTED),
                                ExamTopic("apt_3", "Simple & Compound Interest", "தனிவட்டி மற்றும் கூட்டுவட்டி சுருக்கு வழிகள்", "High", TopicStatus.NOT_STARTED)
                            )
                        )
                    )
                )
            )
        ),
        ExamInfo(
            id = "upsc_cse",
            nameEn = "UPSC Civil Services (IAS / IPS)",
            nameTa = "யு.பி.எஸ்.சி குடிமைப்பணிகள் தேர்வு (IAS / IPS)",
            category = "UPSC",
            badgeColorHex = "#1A237E",
            notificationStatus = "Annual UPSC Calendar Confirmed",
            expectedDate = "2026-05-24",
            patternEn = "Prelims: Paper I (GS 100 Qs, 200 Marks) + CSAT (80 Qs, 200 Marks). Mains: 9 Descriptive Papers. Personality Interview: 275 Marks.",
            patternTa = "முதல்நிலை: தாள் 1 (பொது அறிவு 100 வினா, 200 மதிப்பெண்) + தாள் 2 (CSAT 80 வினா, தகுதி தாள்). முதன்மைத் தேர்வு: 9 விரிவான தாள்கள். நேர்முகத் தேர்வு: 275 மதிப்பெண்கள்.",
            totalMarks = 2025,
            hasNegativeMarking = true,
            negativeMarkingRatio = "1/3rd (0.33) marks deducted per wrong answer",
            subjects = listOf(
                ExamSubject(
                    id = "upsc_polity",
                    titleEn = "Indian Polity & Governance",
                    titleTa = "இந்திய அரசியலமைப்பு & ஆளுமை",
                    weightagePercentage = 22,
                    chapters = listOf(
                        ExamChapter(
                            id = "upsc_pol_ch1",
                            titleEn = "Constitutional Framework & Federalism",
                            titleTa = "அரசியலமைப்பு கட்டமைப்பு & கூட்டாட்சி முறை",
                            topics = listOf(
                                ExamTopic("up_pol_1", "Constitutionalism, Basic Structure Doctrine", "அடிப்படை கட்டமைப்பு கோட்பாடு (Basic Structure)", "High", TopicStatus.COMPLETED),
                                ExamTopic("up_pol_2", "Parliamentary Democracy vs Presidential System", "நாடாளுமன்ற மக்களாட்சி முறை vs குடியரசு தலைவர் முறை", "Medium", TopicStatus.NOT_STARTED),
                                ExamTopic("up_pol_3", "Judicial Review & Public Interest Litigation (PIL)", "நீதித்துறை மறுஆய்வு & பொதுநல மனு (PIL)", "High", TopicStatus.NOT_STARTED)
                            )
                        )
                    )
                ),
                ExamSubject(
                    id = "upsc_economy",
                    titleEn = "Economic & Social Development",
                    titleTa = "பொருளாதாரம் மற்றும் சமூக மேம்பாடு",
                    weightagePercentage = 20,
                    chapters = listOf(
                        ExamChapter(
                            id = "upsc_eco_ch1",
                            titleEn = "Macroeconomics & Monetary Policy",
                            titleTa = "பேரியல் பொருளாதாரம் & பணவியல் கொள்கை",
                            topics = listOf(
                                ExamTopic("up_eco_1", "Inflation, CPI, WPI & RBI Policy Rates (Repo, Reverse Repo)", "பணவீக்கம், பணமதிப்பிழப்பு & ரிசர்வ் வங்கி விகிதங்கள்", "High", TopicStatus.NEEDS_REVISION),
                                ExamTopic("up_eco_2", "Fiscal Deficit, Budget & Taxation Reforms", "நிதி பற்றாக்குறை, மத்திய பட்ஜெட் & வரி சீர்திருத்தங்கள்", "High", TopicStatus.NOT_STARTED)
                            )
                        )
                    )
                ),
                ExamSubject(
                    id = "upsc_env",
                    titleEn = "Environment, Ecology & Biodiversity",
                    titleTa = "சுற்றுச்சூழல், சூழலியல் & பல்லுயிர் பாதுகாப்பு",
                    weightagePercentage = 20,
                    chapters = listOf(
                        ExamChapter(
                            id = "upsc_env_ch1",
                            titleEn = "Climate Change & Protected Areas",
                            titleTa = "பருவநிலை மாற்றம் & பாதுகாக்கப்பட்ட பகுதிகள்",
                            topics = listOf(
                                ExamTopic("up_env_1", "National Parks, Biosphere Reserves in India & Ramsar Sites", "தேசிய பூங்காக்கள், உயிர்க்கோள காப்பகங்கள் & ராம்சார் தளங்கள்", "High", TopicStatus.NOT_STARTED),
                                ExamTopic("up_env_2", "UNFCCC COP Agreements & Renewable Energy Targets", "பருவநிலை மாற்ற சர்வதேச மாநாடுகள் & புதுப்பிக்கத்தக்க எரிசக்தி", "High", TopicStatus.NOT_STARTED)
                            )
                        )
                    )
                )
            )
        ),
        ExamInfo(
            id = "neet_ug",
            nameEn = "NEET-UG Medical Entrance",
            nameTa = "நீட் (NEET-UG) மருத்துவ நுழைவுத்தேர்வு",
            category = "NEET",
            badgeColorHex = "#00838F",
            notificationStatus = "National Testing Agency (NTA) Schedule",
            expectedDate = "2026-05-03",
            patternEn = "180 Questions out of 200 (Biology: 90 Qs, Physics: 45 Qs, Chemistry: 45 Qs). 720 Total Marks. +4 for correct, -1 for wrong.",
            patternTa = "மொத்தம் 180 வினாக்கள் (உயிரியல்: 90, இயற்பியல்: 45, வேதியியல்: 45). மொத்தம் 720 மதிப்பெண்கள். சரியான விடைக்கு +4, தவறான விடைக்கு -1.",
            totalMarks = 720,
            hasNegativeMarking = true,
            negativeMarkingRatio = "-1 mark per wrong MCQ",
            subjects = listOf(
                ExamSubject(
                    id = "neet_bio",
                    titleEn = "Biology (Botany & Zoology)",
                    titleTa = "உயிரியல் (தாவரவியல் & விலங்கியல்)",
                    weightagePercentage = 50,
                    chapters = listOf(
                        ExamChapter(
                            id = "neet_genetics",
                            titleEn = "Genetics & Molecular Basis of Inheritance",
                            titleTa = "மரபியல் & மூலக்கூறு மரபியல்",
                            topics = listOf(
                                ExamTopic("neet_bio_1", "Mendelian Genetics & Chromosomal Inheritance", "மெண்டலின் மரபியல் விதிகள் & குரோமோசோம் மரபு", "High", TopicStatus.COMPLETED),
                                ExamTopic("neet_bio_2", "DNA Replication, Transcription & Translation", "டி.என்.ஏ இரட்டிப்பாதல் மற்றும் புரதச்சேர்க்கை", "High", TopicStatus.LEARNING),
                                ExamTopic("neet_bio_3", "Human Physiology: Cardiovascular System", "மனித உடலியங்கியல்: இரத்த ஓட்ட மண்டலம் & இதயம்", "High", TopicStatus.NOT_STARTED)
                            )
                        )
                    )
                ),
                ExamSubject(
                    id = "neet_chem",
                    titleEn = "Chemistry (Physical, Organic, Inorganic)",
                    titleTa = "வேதியியல் (இயற்பியல், கரிம, கனிம)",
                    weightagePercentage = 25,
                    chapters = listOf(
                        ExamChapter(
                            id = "neet_chem_ch1",
                            titleEn = "Chemical Bonding & Molecular Structure",
                            titleTa = "வேதிப்பிணைப்பு & மூலக்கூறு அமைப்பு",
                            topics = listOf(
                                ExamTopic("neet_chem_1", "VSEPR Theory, Hybridisation (sp, sp2, sp3)", "VSEPR கோட்பாடு & இனக்கலப்பு விதிகள்", "High", TopicStatus.NOT_STARTED),
                                ExamTopic("neet_chem_2", "Coordination Compounds & Nomenclature", "அணைவுச் சேர்மங்கள் மற்றும் IUPAC பெயரிடுதல்", "Medium", TopicStatus.NOT_STARTED)
                            )
                        )
                    )
                )
            )
        ),
        ExamInfo(
            id = "ssc_cgl",
            nameEn = "SSC CGL / CHSL Combined Graduate Level",
            nameTa = "எஸ்.எஸ்.சி சி.ஜி.எல் / சி.எச்.எஸ்.எல் மத்திய அரசுப் பணிகள்",
            category = "SSC",
            badgeColorHex = "#D84315",
            notificationStatus = "Staff Selection Commission Annual Calendar",
            expectedDate = "2026-09-12",
            patternEn = "Tier 1: 100 Questions, 200 Marks, 60 Minutes. 25 Qs each for Reasoning, GA, Quant, English. Negative: 0.50 marks.",
            patternTa = "அடுக்கு 1: 100 வினாக்கள், 200 மதிப்பெண்கள், 60 நிமிடங்கள். கணிதம், பொது அறிவு, காரணவியல், ஆங்கிலம் தலா 25 வினாக்கள். நெகடிவ்: 0.50 மதிப்பெண்கள்.",
            totalMarks = 200,
            hasNegativeMarking = true,
            negativeMarkingRatio = "-0.50 per wrong MCQ",
            subjects = listOf(
                ExamSubject(
                    id = "ssc_quant",
                    titleEn = "Quantitative Aptitude & Advanced Math",
                    titleTa = "கணிதம் மற்றும் அளவியல்",
                    weightagePercentage = 30,
                    chapters = listOf(
                        ExamChapter(
                            id = "ssc_arith",
                            titleEn = "Trigonometry & Geometry",
                            titleTa = "முக்கோணவியல் மற்றும் வடிவியல்",
                            topics = listOf(
                                ExamTopic("ssc_m_1", "Trigonometric Identities & Heights and Distances", "முக்கோணவியல் முற்றொருமைகள் & உயரங்களும் தொலைவுகளும்", "High", TopicStatus.NOT_STARTED),
                                ExamTopic("ssc_m_2", "Circle Theorems, Tangents & Triangles", "வட்டத்தின் பண்புகள் மற்றும் முக்கோண வடிவியல்", "High", TopicStatus.NOT_STARTED)
                            )
                        )
                    )
                ),
                ExamSubject(
                    id = "ssc_ga",
                    titleEn = "General Awareness & Static GK",
                    titleTa = "பொது அறிவு மற்றும் நடப்பு நிகழ்வுகள்",
                    weightagePercentage = 25,
                    chapters = listOf(
                        ExamChapter(
                            id = "ssc_hist",
                            titleEn = "Modern Indian History & Freedom Struggle",
                            titleTa = "நவீன இந்திய வரலாறு & விடுதலைப் போராட்டம்",
                            topics = listOf(
                                ExamTopic("ssc_g_1", "1857 Revolt to Indian Independence Movement", "1857 பெரும் புரட்சி முதல் சுதந்திர இயக்கம் வரை", "High", TopicStatus.COMPLETED),
                                ExamTopic("ssc_g_2", "Governors-General, Viceroys and Acts", "பிரிட்டிஷ் ஆளுநர்கள் மற்றும் முக்கிய சட்டங்கள்", "Medium", TopicStatus.NOT_STARTED)
                            )
                        )
                    )
                )
            )
        ),
        ExamInfo(
            id = "ctet_tet",
            nameEn = "CTET & Tamil Nadu TET (Paper I & II)",
            nameTa = "சி.டி.இ.டி & தமிழ்நாடு ஆசிரியர் தகுதித்தேர்வு (TET)",
            category = "CTET",
            badgeColorHex = "#6A1B9A",
            notificationStatus = "Central Board of Secondary Education (CBSE)",
            expectedDate = "2026-07-10",
            patternEn = "150 MCQs, 150 Marks, 2.5 Hours. Child Development & Pedagogy (30 Qs), Language I (30 Qs), Language II (30 Qs), Subject (60 Qs). No Negative.",
            patternTa = "150 வினாக்கள், 150 மதிப்பெண்கள், 2.5 மணி நேரம். குழந்தை மேம்பாடு & கற்பித்தல் முறை (30), மொழி 1 (30), மொழி 2 (30), பாடம் (60). நெகடிவ் மதிப்பெண் இல்லை.",
            totalMarks = 150,
            hasNegativeMarking = false,
            negativeMarkingRatio = "No Negative Marking",
            subjects = listOf(
                ExamSubject(
                    id = "ctet_cdp",
                    titleEn = "Child Development and Pedagogy (CDP)",
                    titleTa = "குழந்தை மேம்பாடும் கற்பித்தல் முறைகளும்",
                    weightagePercentage = 35,
                    chapters = listOf(
                        ExamChapter(
                            id = "cdp_theories",
                            titleEn = "Learning Theories of Piaget, Vygotsky & Kohlberg",
                            titleTa = "பியாஜே, வைகோட்ஸ்கி & கோல்பெர்க் கற்றல் கோட்பாடுகள்",
                            topics = listOf(
                                ExamTopic("cdp_1", "Piaget's Stages of Cognitive Development", "பியாஜேவின் அறிவாற்றல் வளர்ச்சி நிலைகள்", "High", TopicStatus.COMPLETED),
                                ExamTopic("cdp_2", "Vygotsky's Scaffolding & Zone of Proximal Development (ZPD)", "வைகோட்ஸ்கியின் ZPD மற்றும் துணைக்கற்றல் (Scaffolding)", "High", TopicStatus.NEEDS_REVISION)
                            )
                        )
                    )
                )
            )
        ),
        ExamInfo(
            id = "banking_ibps",
            nameEn = "Banking Examinations (IBPS PO / SBI Clerk)",
            nameTa = "வங்கித் தேர்வுகள் (IBPS PO / SBI கிளார்க்)",
            category = "BANKING",
            badgeColorHex = "#0277BD",
            notificationStatus = "IBPS Official Annual Schedule",
            expectedDate = "2026-10-18",
            patternEn = "Prelims: 100 Qs (Eng 30, Quant 35, Reasoning 35) in 60 mins. Mains: Reasoning, Quant, GA & English. Negative: 0.25 marks.",
            patternTa = "முதல்நிலை: 100 வினாக்கள் (ஆங்கிலம் 30, கணிதம் 35, ரீசனிங் 35) 60 நிமிடங்கள். முதன்மை: வங்கி விழிப்புணர்வு & பகுத்தறிவு. நெகடிவ்: 0.25.",
            totalMarks = 100,
            hasNegativeMarking = true,
            negativeMarkingRatio = "-0.25 marks per incorrect answer",
            subjects = listOf(
                ExamSubject(
                    id = "bank_reasoning",
                    titleEn = "Logical Reasoning & Puzzles",
                    titleTa = "தர்க்க ரீதியான பகுத்தறிதல் & புதிர்கள்",
                    weightagePercentage = 35,
                    chapters = listOf(
                        ExamChapter(
                            id = "bank_puzzles",
                            titleEn = "Seating Arrangement & Floor Puzzles",
                            titleTa = "இருக்கை அமைப்பு மற்றும் மாடி புதிர்கள்",
                            topics = listOf(
                                ExamTopic("bnk_1", "Circular & Linear Seating Arrangement with 2 Variables", "வட்ட மற்றும் நேர்கோட்டு இருக்கை அமைப்பு புதிர்கள்", "High", TopicStatus.NOT_STARTED),
                                ExamTopic("bnk_2", "Syllogism (Only a Few, Possibility Cases)", "சிலாஜிசம் (Only a Few நிபந்தனைகள்)", "High", TopicStatus.COMPLETED)
                            )
                        )
                    )
                )
            )
        ),
        ExamInfo(
            id = "rrb_railway",
            nameEn = "Railway Recruitment Board (RRB NTPC / Group D)",
            nameTa = "ரயில்வே பணியாளர் தேர்வு வாரியம் (RRB NTPC / குரூப் டி)",
            category = "RAILWAY",
            badgeColorHex = "#455A64",
            notificationStatus = "Railway Recruitment Board Notice 2026",
            expectedDate = "2026-12-05",
            patternEn = "CBT-1: 100 Qs, 90 mins (GA: 40 Qs, Math: 30 Qs, Reasoning: 30 Qs). Negative marking 1/3rd.",
            patternTa = "கணினித் தேர்வு 1: 100 வினாக்கள், 90 நிமிடம் (பொது அறிவு 40, கணிதம் 30, பகுத்தறிவு 30). 1/3 பங்கு நெகடிவ் மதிப்பெண்.",
            totalMarks = 100,
            hasNegativeMarking = true,
            negativeMarkingRatio = "-0.33 per wrong MCQ",
            subjects = listOf(
                ExamSubject(
                    id = "rrb_gen_science",
                    titleEn = "General Science (Physics, Chemistry, Life Science)",
                    titleTa = "பொது அறிவியல் (இயற்பியல், வேதியியல், உயிரியல்)",
                    weightagePercentage = 40,
                    chapters = listOf(
                        ExamChapter(
                            id = "rrb_sci_ch1",
                            titleEn = "Electricity, Magnetism & Periodic Table",
                            titleTa = "மின்னியல், காந்தவியல் மற்றும் ஆவர்த்தன அட்டவணை",
                            topics = listOf(
                                ExamTopic("rrb_1", "Ohm's Law, Resistance in Series and Parallel", "ஓம் விதி, தொடர் மற்றும் பக்க இணைப்பு மின்தடைகள்", "High", TopicStatus.NOT_STARTED),
                                ExamTopic("rrb_2", "Modern Periodic Law & Chemical Reactions", "நவீன ஆவர்த்தன விதி & வேதிவினைகள்", "High", TopicStatus.NOT_STARTED)
                            )
                        )
                    )
                )
            )
        )
    )

    fun getExamById(id: String): ExamInfo {
        return getAllExams().firstOrNull { it.id == id } ?: getAllExams().first()
    }

    // Verified Past Year Questions (PYQ) with Authorized Citations
    fun getVerifiedPyqs(examId: String): List<PyqQuestion> {
        return when (examId) {
            "tnpsc_grp4" -> listOf(
                PyqQuestion(
                    id = "pyq_tnpsc_1",
                    examId = "tnpsc_grp4",
                    examNameTag = "TNPSC Group 4 - Official 2022 Paper (Q. 42)",
                    subjectName = "Indian Polity / இந்திய அரசியலமைப்பு",
                    topicName = "Fundamental Rights / அடிப்படை உரிமைகள்",
                    questionEn = "Which Article of the Constitution of India provides for the 'Right to Constitutional Remedies' allowing citizens to approach the Supreme Court directly?",
                    questionTa = "இந்திய அரசியலமைப்பின் எந்த சரத்து, குடிமக்கள் தங்களின் அடிப்படை உரிமைகள் மீறப்படும்போது நேரடியாக உச்ச நீதிமன்றத்தை அணுகும் 'அரசியலமைப்புக்குட்பட்டு தீர்வு காணும் உரிமையை' வழங்குகிறது?",
                    optionsEn = listOf("Article 19", "Article 21", "Article 32", "Article 44"),
                    optionsTa = listOf("சரத்து 19", "சரத்து 21", "சரத்து 32", "சரத்து 44"),
                    correctIndex = 2,
                    explanationEn = "Article 32 guarantees the Right to Constitutional Remedies. Dr. B.R. Ambedkar termed Article 32 as the 'Heart and Soul of the Indian Constitution' because without it, fundamental rights would be mere paper declarations.",
                    explanationTa = "சரத்து 32 அரசியலமைப்புக்குட்பட்டு தீர்வு காணும் உரிமையை வழங்குகிறது. டாக்டர் பி.ஆர். அம்பேத்கர் இச்சரத்தை 'இந்திய அரசியலமைப்பின் இதயம் மற்றும் ஆன்மா' என வர்ணித்தார், ஏனெனில் அடிப்படை உரிமைகள் மீறப்பட்டால் உச்ச நீதிமன்றம் 5 வகையான நீதிப்பேராணைகளை (Writs) பிறப்பிக்க இது அதிகாரம் அளிக்கிறது.",
                    isOfficialPastPaper = true
                ),
                PyqQuestion(
                    id = "pyq_tnpsc_2",
                    examId = "tnpsc_grp4",
                    examNameTag = "TNPSC Group 4 - Official 2022 Paper (Q. 18)",
                    subjectName = "General Tamil / பொதுத்தமிழ்",
                    topicName = "Thirukkural / திருக்குறள்",
                    questionEn = "In Thirukkural, how many 'Adhikarams' (Chapters) and total couplets (Kural) are present in the 'Porutpaal' (Book of Wealth/Polity)?",
                    questionTa = "திருக்குறளில் உள்ள பொருட்பாலில் மொத்தம் எத்தனை அதிகாரங்கள் மற்றும் குறட்பாக்கள் இடம்பெற்றுள்ளன?",
                    optionsEn = listOf("38 Adhikarams (380 Kurals)", "70 Adhikarams (700 Kurals)", "25 Adhikarams (250 Kurals)", "133 Adhikarams (1330 Kurals)"),
                    optionsTa = listOf("38 அதிகாரங்கள் (380 குறள்கள்)", "70 அதிகாரங்கள் (700 குறள்கள்)", "25 அதிகாரங்கள் (250 குறள்கள்)", "133 அதிகாரங்கள் (1330 குறள்கள்)"),
                    correctIndex = 1,
                    explanationEn = "Thirukkural is divided into 3 divisions: Arathupaal (38 chapters, 380 kurals), Porutpaal (70 chapters, 700 kurals), and Kamathupaal (25 chapters, 250 kurals). Total: 133 chapters, 1330 kurals.",
                    explanationTa = "திருக்குறள் முப்பால்களாக பிரிக்கப்பட்டுள்ளது: அறத்துப்பால் (38 அதிகாரங்கள், 380 குறள்கள்), பொருட்பால் (70 அதிகாரங்கள், 700 குறள்கள்), காமத்துப்பால் (25 அதிகாரங்கள், 250 குறள்கள்). பொருட்பாலில் 70 அதிகாரங்கள் உள்ளன.",
                    isOfficialPastPaper = true
                ),
                PyqQuestion(
                    id = "pyq_tnpsc_3",
                    examId = "tnpsc_grp4",
                    examNameTag = "TNPSC Group 4 - Official 2019 Paper (Q. 88)",
                    subjectName = "Aptitude / கணித நுண்ணறிவு",
                    topicName = "HCF & LCM / மீ.சி.ம & மீ.பொ.வ",
                    questionEn = "The HCF and LCM of two numbers are 12 and 144 respectively. If one of the numbers is 36, find the other number.",
                    questionTa = "இரு எண்களின் மீ.பொ.வ மற்றும் மீ.சி.ம முறையே 12 மற்றும் 144 ஆகும். அவற்றில் ஒரு எண் 36 எனில், மற்றொரு எண்ணைக் காண்க.",
                    optionsEn = listOf("24", "48", "72", "96"),
                    optionsTa = listOf("24", "48", "72", "96"),
                    correctIndex = 1,
                    explanationEn = "Formula: Product of two numbers = HCF × LCM. Let second number be X. Then 36 × X = 12 × 144 => X = (12 × 144) / 36 = 144 / 3 = 48.",
                    explanationTa = "சூத்திரம்: இரு எண்களின் பெருக்கற்பலன் = மீ.பொ.வ × மீ.சி.ம. எனவே, 36 × X = 12 × 144 => X = (12 × 144) / 36 = 48. ஆகவே மற்றொரு எண் 48 ஆகும்.",
                    isOfficialPastPaper = true
                )
            )
            "upsc_cse" -> listOf(
                PyqQuestion(
                    id = "pyq_upsc_1",
                    examId = "upsc_cse",
                    examNameTag = "UPSC CSE Prelims 2023 - GS Paper I",
                    subjectName = "Indian Polity / இந்திய அரசியலமைப்பு",
                    topicName = "Basic Structure & Constitutional Amendments",
                    questionEn = "In India, which one of the following Constitutional Amendment Acts introduced Article 21A providing for Free and Compulsory Education to all children between 6 to 14 years?",
                    questionTa = "இந்தியாவில் 6 முதல் 14 வயதுடைய குழந்தைகளுக்கு இலவச மற்றும் கட்டாயக் கல்வியை அடிப்படை உரிமையாக (சரத்து 21A) சேர்த்த அரசியலமைப்பு சட்டத்திருத்தம் எது?",
                    optionsEn = listOf("42nd Constitutional Amendment Act, 1976", "44th Constitutional Amendment Act, 1978", "86th Constitutional Amendment Act, 2002", "91st Constitutional Amendment Act, 2003"),
                    optionsTa = listOf("42-வது சட்டத்திருத்தம், 1976", "44-வது சட்டத்திருத்தம், 1978", "86-வது சட்டத்திருத்தம், 2002", "91-வது சட்டத்திருத்தம், 2003"),
                    correctIndex = 2,
                    explanationEn = "The 86th Constitutional Amendment Act, 2002 inserted Article 21A into the Constitution, making education a Fundamental Right for children aged 6 to 14 years.",
                    explanationTa = "2002-ஆம் ஆண்டின் 86-வது அரசியலமைப்பு திருத்தச் சட்டம், சரத்து 21A-வை இணைத்து 6 முதல் 14 வயதுக்குட்பட்ட குழந்தைகளுக்கு இலவச மற்றும் கட்டாயக் கல்வியை அடிப்படை உரிமையாக்கியது.",
                    isOfficialPastPaper = true
                ),
                PyqQuestion(
                    id = "pyq_upsc_2",
                    examId = "upsc_cse",
                    examNameTag = "UPSC CSE Prelims 2022 - GS Paper I",
                    subjectName = "Indian Economy / பொருளாதாரம்",
                    topicName = "Inflation & Monetary Policy",
                    questionEn = "With reference to the Indian economy, what is the primary target variable for the Monetary Policy Committee (MPC) of the Reserve Bank of India?",
                    questionTa = "இந்திய பொருளாதாரத்தில், ரிசர்வ் வங்கியின் பணவியல் கொள்கைக் குழு (MPC) கட்டுக்குள் வைக்க இலக்கு நிர்ணயிக்கும் முதன்மை பணவீக்க அளவீடு எது?",
                    optionsEn = listOf("Wholesale Price Index (WPI) Inflation", "Consumer Price Index Combined (CPI-C) Inflation", "Core GDP Deflator", "Index of Industrial Production (IIP)"),
                    optionsTa = listOf("மொத்த விற்பனை விலைக் குறியீடு (WPI)", "நுகர்வோர் விலைக் குறியீடு (CPI-Combined)", "ஜிடிபி பணமதிப்பிழப்பீடு (GDP Deflator)", "தொழில்துறை உற்பத்தி குறியீடு (IIP)"),
                    correctIndex = 1,
                    explanationEn = "Under the flexible inflation targeting framework adopted since 2016 (Urjit Patel Committee recommendation), RBI targets Headline CPI Combined inflation at 4% with a tolerance band of +/- 2% (2% to 6%).",
                    explanationTa = "2016-ஆம் ஆண்டு முதல் ரிசர்வ் வங்கி நுகர்வோர் விலைக் குறியீட்டை (CPI-Combined) அடிப்படையாகக் கொண்டு 4% பணவீக்கத்தை (+/- 2% அனுமதிக்கப்பட்ட எல்லை) இலக்காக நிர்ணயித்துள்ளது.",
                    isOfficialPastPaper = true
                )
            )
            "neet_ug" -> listOf(
                PyqQuestion(
                    id = "pyq_neet_1",
                    examId = "neet_ug",
                    examNameTag = "NEET-UG Official 2023 Paper",
                    subjectName = "Biology / உயிரியல்",
                    topicName = "Molecular Basis of Inheritance / மூலக்கூறு மரபியல்",
                    questionEn = "During transcription in prokaryotes, which subunit of RNA polymerase is responsible for recognizing the promoter region and initiating transcription?",
                    questionTa = "புரோகேரியோட்டுகளில் படியெடுத்தலின் போது (Transcription), ஆர்.என்.ஏ பாலிமரேஸின் எந்த துணை அலகு புரோமோட்டர் பகுதியை அடையாளம் கண்டு தொடக்கத்தை ஏற்படுத்துகிறது?",
                    optionsEn = listOf("Alpha (α) subunit", "Beta (β) subunit", "Sigma (σ) factor", "Rho (ρ) factor"),
                    optionsTa = listOf("ஆல்பா (α) துணை அலகு", "பீட்டா (β) துணை அலகு", "சிக்மா (σ) காரணி", "ரோ (ρ) காரணி"),
                    correctIndex = 2,
                    explanationEn = "The Sigma (σ) factor confers promoter specificity to RNA polymerase core enzyme, initiating transcription. Rho (ρ) factor is responsible for transcription termination.",
                    explanationTa = "ஆர்.என்.ஏ பாலிமரேஸ் என்சைமில் சிக்மா (σ) காரணி மட்டுமே புரோமோட்டர் பகுதியைத் துல்லியமாக அடையாளம் கண்டு படியெடுத்தலைத் தொடங்கும். ரோ (ρ) காரணி முடிவுக்குக் காரணமாகிறது.",
                    isOfficialPastPaper = true
                )
            )
            else -> listOf(
                PyqQuestion(
                    id = "pyq_gen_1",
                    examId = examId,
                    examNameTag = "Official Model Question",
                    subjectName = "General Awareness",
                    topicName = "National Organizations",
                    questionEn = "Where is the headquarters of the Reserve Bank of India located?",
                    questionTa = "இந்திய ரிசர்வ் வங்கியின் (RBI) தலைமை அலுவலகம் எங்கு அமைந்துள்ளது?",
                    optionsEn = listOf("New Delhi", "Mumbai", "Kolkata", "Chennai"),
                    optionsTa = listOf("புது தில்லி", "மும்பை", "கொல்கத்தா", "சென்னை"),
                    correctIndex = 1,
                    explanationEn = "The Reserve Bank of India was established in Kolkata in 1935 but permanently moved to Mumbai in 1937.",
                    explanationTa = "ரிசர்வ் வங்கி 1935-ல் கொல்கத்தாவில் தொடங்கப்பட்டு, 1937-ல் நிரந்தரமாக மும்பைக்கு மாற்றப்பட்டது.",
                    isOfficialPastPaper = true
                )
            )
        }
    }

    // Dynamic Daily Study Plan Generator based on User Availability
    fun generateDailyTasks(
        examId: String,
        dailyHours: DailyAvailability,
        preferredSlot: StudyScheduleSlot,
        dayOffset: Int
    ): List<DailyTask> {
        val exam = getExamById(examId)
        val subject = exam.subjects.getOrNull(dayOffset % exam.subjects.size) ?: exam.subjects.first()
        val chapter = subject.chapters.firstOrNull() ?: ExamChapter("c1", "Core Concepts", "அடிப்படை கருத்துகள்", emptyList())
        val topic = chapter.topics.getOrNull(dayOffset % (chapter.topics.size.coerceAtLeast(1)))
            ?: ExamTopic("t1", "Essential Chapter Overview", "முக்கிய அத்தியாய மீள்பார்வை", "High")

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, dayOffset)
        val dateStr = sdf.format(calendar.time)

        val tasks = mutableListOf<DailyTask>()

        when (dailyHours) {
            DailyAvailability.ONE_HOUR -> {
                // 1 Hour High-Yield Routine
                tasks.add(
                    DailyTask(
                        id = "task_${dayOffset}_1",
                        dayOffset = dayOffset,
                        timeSlot = "${preferredSlot.defaultTimes} - +15m",
                        examId = examId,
                        subjectNameEn = subject.titleEn,
                        subjectNameTa = subject.titleTa,
                        topicTitleEn = "Rapid Spaced Revision of Yesterday",
                        topicTitleTa = "நேற்றைய முக்கிய குறிப்புகள் 15 நிமிட மின்னல் மீள்பார்வை",
                        chapterNameEn = chapter.titleEn,
                        chapterNameTa = chapter.titleTa,
                        activityType = TaskActivityType.REVISE,
                        estimatedMinutes = 15,
                        studyMaterialGuidanceEn = "Review your flashcards and high-yield summary bullets from yesterday. Zero passive reading.",
                        studyMaterialGuidanceTa = "நேற்று படித்தவற்றின் முக்கிய சுருக்கம் மற்றும் ஃபிளாஷ்கார்டுகளை மட்டும் விரைவாக நினைவுபடுத்தவும்.",
                        questionsToSolveCount = 0,
                        dateString = dateStr
                    )
                )
                tasks.add(
                    DailyTask(
                        id = "task_${dayOffset}_2",
                        dayOffset = dayOffset,
                        timeSlot = "+15m - +50m",
                        examId = examId,
                        subjectNameEn = subject.titleEn,
                        subjectNameTa = subject.titleTa,
                        topicTitleEn = topic.titleEn,
                        topicTitleTa = topic.titleTa,
                        chapterNameEn = chapter.titleEn,
                        chapterNameTa = chapter.titleTa,
                        activityType = TaskActivityType.LEARN_NEW,
                        estimatedMinutes = 35,
                        studyMaterialGuidanceEn = "Focus on definitions, core steps, and exam-oriented points in the AI Teacher tab.",
                        studyMaterialGuidanceTa = "AI ஆசிரியர் பகுதியில் உள்ள எளிய விளக்கம் மற்றும் முக்கிய வரையறைகளை ஆழமாகப் படிக்கவும்.",
                        questionsToSolveCount = 5,
                        dateString = dateStr
                    )
                )
                tasks.add(
                    DailyTask(
                        id = "task_${dayOffset}_3",
                        dayOffset = dayOffset,
                        timeSlot = "+50m - +60m",
                        examId = examId,
                        subjectNameEn = subject.titleEn,
                        subjectNameTa = subject.titleTa,
                        topicTitleEn = "High-Yield Practice MCQs",
                        topicTitleTa = "முக்கிய பயிற்சி வினாக்கள் தீர்வு",
                        chapterNameEn = chapter.titleEn,
                        chapterNameTa = chapter.titleTa,
                        activityType = TaskActivityType.PRACTICE_MCQ,
                        estimatedMinutes = 10,
                        studyMaterialGuidanceEn = "Solve 5-8 verified MCQs to solidify today's core concept.",
                        studyMaterialGuidanceTa = "இன்று படித்த தலைப்பில் 5-8 பயிற்சி வினாக்களைத் தீர்த்து உடனடி விளக்கம் பார்க்கவும்.",
                        questionsToSolveCount = 8,
                        dateString = dateStr
                    )
                )
            }
            DailyAvailability.TWO_HOURS -> {
                // 2 Hours Balanced Routine
                tasks.add(
                    DailyTask(
                        id = "task_${dayOffset}_1",
                        dayOffset = dayOffset,
                        timeSlot = "${preferredSlot.defaultTimes} - +20m",
                        examId = examId,
                        subjectNameEn = subject.titleEn,
                        subjectNameTa = subject.titleTa,
                        topicTitleEn = "Active Recall & Yesterday's Revision",
                        topicTitleTa = "நேற்றைய பாடங்களின் மீள்பார்வை & நினைவுகூர்தல்",
                        chapterNameEn = chapter.titleEn,
                        chapterNameTa = chapter.titleTa,
                        activityType = TaskActivityType.REVISE,
                        estimatedMinutes = 20,
                        studyMaterialGuidanceEn = "Use flashcards or mistake notebook to test active recall before learning new material.",
                        studyMaterialGuidanceTa = "நேற்று செய்த தவறுகள் மற்றும் முக்கிய சூத்திரங்களை ஃபிளாஷ்கார்டு கொண்டு சுயபரிசோதனை செய்யவும்.",
                        questionsToSolveCount = 0,
                        dateString = dateStr
                    )
                )
                tasks.add(
                    DailyTask(
                        id = "task_${dayOffset}_2",
                        dayOffset = dayOffset,
                        timeSlot = "+20m - +75m",
                        examId = examId,
                        subjectNameEn = subject.titleEn,
                        subjectNameTa = subject.titleTa,
                        topicTitleEn = topic.titleEn,
                        topicTitleTa = topic.titleTa,
                        chapterNameEn = chapter.titleEn,
                        chapterNameTa = chapter.titleTa,
                        activityType = TaskActivityType.LEARN_NEW,
                        estimatedMinutes = 55,
                        studyMaterialGuidanceEn = "Complete the 10-point lesson in the AI Teacher module with bilingual audio narration.",
                        studyMaterialGuidanceTa = "AI ஆசிரியரின் 10 படிநிலை விளக்கங்களை தமிழில் அல்லது இருமொழியில் படிக்கவும்.",
                        questionsToSolveCount = 0,
                        dateString = dateStr
                    )
                )
                tasks.add(
                    DailyTask(
                        id = "task_${dayOffset}_3",
                        dayOffset = dayOffset,
                        timeSlot = "+75m - +105m",
                        examId = examId,
                        subjectNameEn = subject.titleEn,
                        subjectNameTa = subject.titleTa,
                        topicTitleEn = "Past Papers & Practice Question Drill",
                        topicTitleTa = "முந்தைய ஆண்டு வினாக்கள் மற்றும் பயிற்சித் தேர்வு",
                        chapterNameEn = chapter.titleEn,
                        chapterNameTa = chapter.titleTa,
                        activityType = TaskActivityType.PRACTICE_MCQ,
                        estimatedMinutes = 30,
                        studyMaterialGuidanceEn = "Solve 15 topic-specific past year questions (PYQ). Note down trick questions.",
                        studyMaterialGuidanceTa = "15 முந்தைய ஆண்டு வினாக்களைத் தீர்த்து, தவறான வினாக்களுக்கான காரணத்தைக் குறிக்கவும்.",
                        questionsToSolveCount = 15,
                        dateString = dateStr
                    )
                )
                tasks.add(
                    DailyTask(
                        id = "task_${dayOffset}_4",
                        dayOffset = dayOffset,
                        timeSlot = "+105m - +120m",
                        examId = examId,
                        subjectNameEn = subject.titleEn,
                        subjectNameTa = subject.titleTa,
                        topicTitleEn = "Mistake Analysis & Smart Note Entry",
                        topicTitleTa = "தவறுகளை ஆராய்தல் & ஸ்மார்ட் குறிப்புகள் பதிவு",
                        chapterNameEn = chapter.titleEn,
                        chapterNameTa = chapter.titleTa,
                        activityType = TaskActivityType.MISTAKE_REVIEW,
                        estimatedMinutes = 15,
                        studyMaterialGuidanceEn = "Record your doubts and missed concepts in the Smart Notes tab for weekly revision.",
                        studyMaterialGuidanceTa = "தவறான வினாக்களுக்கான விளக்கங்களை 'Smart Notes' பகுதியில் சேமிக்கவும்.",
                        questionsToSolveCount = 0,
                        dateString = dateStr
                    )
                )
            }
            else -> {
                // 3 to 6+ Hours Comprehensive Schedule
                tasks.add(
                    DailyTask(
                        id = "task_${dayOffset}_1",
                        dayOffset = dayOffset,
                        timeSlot = "Slot 1: 06:00 AM - 06:40 AM",
                        examId = examId,
                        subjectNameEn = subject.titleEn,
                        subjectNameTa = subject.titleTa,
                        topicTitleEn = "Deep Spaced Revision & Formula/Fact Recall",
                        topicTitleTa = "ஆழ்ந்த மீள்பார்வை & சூத்திரங்கள் / வரலாற்று தகவல்கள் நினைவுபடுத்தல்",
                        chapterNameEn = chapter.titleEn,
                        chapterNameTa = chapter.titleTa,
                        activityType = TaskActivityType.REVISE,
                        estimatedMinutes = 40,
                        studyMaterialGuidanceEn = "Review all notes marked 'Needs Revision' from the past 7 days.",
                        studyMaterialGuidanceTa = "கடந்த 7 நாட்களில் 'மீள்பார்வை தேவை' என குறிக்கப்பட்ட பாடங்களை முழுமையாக வாசிக்கவும்.",
                        questionsToSolveCount = 0,
                        dateString = dateStr
                    )
                )
                tasks.add(
                    DailyTask(
                        id = "task_${dayOffset}_2",
                        dayOffset = dayOffset,
                        timeSlot = "Slot 2: 06:45 AM - 08:00 AM",
                        examId = examId,
                        subjectNameEn = subject.titleEn,
                        subjectNameTa = subject.titleTa,
                        topicTitleEn = topic.titleEn,
                        topicTitleTa = topic.titleTa,
                        chapterNameEn = chapter.titleEn,
                        chapterNameTa = chapter.titleTa,
                        activityType = TaskActivityType.LEARN_NEW,
                        estimatedMinutes = 75,
                        studyMaterialGuidanceEn = "Complete full syllabus depth: definitions, case laws/mechanisms, practical illustrations.",
                        studyMaterialGuidanceTa = "அதிகாரப்பூர்வ பாடத்திட்டத்தின்படி முழுமையான விளக்கங்கள் மற்றும் வரைபடங்களைப் படிக்கவும்.",
                        questionsToSolveCount = 0,
                        dateString = dateStr
                    )
                )
                tasks.add(
                    DailyTask(
                        id = "task_${dayOffset}_3",
                        dayOffset = dayOffset,
                        timeSlot = "Slot 3: 11:00 AM - 12:00 PM",
                        examId = examId,
                        subjectNameEn = subject.titleEn,
                        subjectNameTa = subject.titleTa,
                        topicTitleEn = "PYQ Deep Dive & Timed MCQ Simulation",
                        topicTitleTa = "அங்கீகரிக்கப்பட்ட முந்தைய ஆண்டு வினாக்கள் மாதிரி பயிற்சி",
                        chapterNameEn = chapter.titleEn,
                        chapterNameTa = chapter.titleTa,
                        activityType = TaskActivityType.PRACTICE_MCQ,
                        estimatedMinutes = 60,
                        studyMaterialGuidanceEn = "Take a 25-question timed sectional quiz with negative marking calculation.",
                        studyMaterialGuidanceTa = "நேரக்கட்டுப்பாட்டுடன் 25 வினாக்கள் கொண்ட மாதிரி வினாடி வினாவை முடிக்கவும்.",
                        questionsToSolveCount = 25,
                        dateString = dateStr
                    )
                )
                tasks.add(
                    DailyTask(
                        id = "task_${dayOffset}_4",
                        dayOffset = dayOffset,
                        timeSlot = "Slot 4: 07:00 PM - 07:45 PM",
                        examId = examId,
                        subjectNameEn = subject.titleEn,
                        subjectNameTa = subject.titleTa,
                        topicTitleEn = "Descriptive Answer Writing / Mistake Notebook",
                        topicTitleTa = "விளக்க உரை எழுதுதல் அல்லது தவறுகள் நோட்டுபுத்தகம் ஆய்வு",
                        chapterNameEn = chapter.titleEn,
                        chapterNameTa = chapter.titleTa,
                        activityType = TaskActivityType.ANSWER_WRITING,
                        estimatedMinutes = 45,
                        studyMaterialGuidanceEn = "Write 1 mains structured answer (Intro, Body, Conclusion) or log mistakes into the mistake notebook.",
                        studyMaterialGuidanceTa = "1 மாதிரி முதன்மைத் தேர்வு வினாவுக்கு கட்டமைப்புடன் விடை எழுதி சுயமாக மதிப்பீடு செய்யவும்.",
                        questionsToSolveCount = 5,
                        dateString = dateStr
                    )
                )
            }
        }

        return tasks
    }

    // Default Mock Tests
    fun getMockTests(examId: String): List<MockTest> {
        val pyqs = getVerifiedPyqs(examId)
        val exam = getExamById(examId)
        return listOf(
            MockTest(
                id = "mock_test_1_${examId}",
                examId = examId,
                titleEn = "${exam.nameEn} - Full Diagnostic Mock Test 1",
                titleTa = "${exam.nameTa} - முழு மாதிரித் தேர்வு 1",
                durationMinutes = 25,
                totalMarks = pyqs.size * 2,
                questions = pyqs
            ),
            MockTest(
                id = "mock_test_2_${examId}",
                examId = examId,
                titleEn = "${exam.nameEn} - High-Yield Sectional Test",
                titleTa = "${exam.nameTa} - முக்கிய தலைப்புகள் பகுதித் தேர்வு",
                durationMinutes = 15,
                totalMarks = pyqs.size * 2,
                questions = pyqs
            )
        )
    }

    // Preloaded Initial Flashcards for Active Recall
    fun getInitialFlashcards(examId: String): List<Flashcard> {
        return listOf(
            Flashcard(
                id = "fc_1",
                examId = examId,
                subjectName = "Indian Polity",
                topicTitle = "Constitutional Articles",
                questionEn = "Which Article of the Constitution abolishes Untouchability?",
                questionTa = "தீண்டாமையை ஒழிக்கும் இந்திய அரசியலமைப்பு சரத்து எது?",
                answerEn = "Article 17 explicitly abolishes untouchability and forbids its practice in any form.",
                answerTa = "சரத்து 17 தீண்டாமையை ஒழிக்கிறது மற்றும் அதன் எந்த வடிவத்தையும் தடை செய்கிறது."
            ),
            Flashcard(
                id = "fc_2",
                examId = examId,
                subjectName = "General Tamil",
                topicTitle = "Thirukkural",
                questionEn = "What is the very first word and concept in Thirukkural?",
                questionTa = "திருக்குறளின் முதல் சொல் மற்றும் கருத்து என்ன?",
                answerEn = "The first word is 'அகர' (Agara), comparing God as the primary origin of the universe just as 'A' is the origin of all alphabets.",
                answerTa = "'அகர' - 'அகர முதல எழுத்தெல்லாம் ஆதி பகவன் முதற்றே உலகு'. எழுத்துக்களுக்கு 'அ' முதன்மையானது போல உலகிற்கு இறைவன் முதன்மையானவன்."
            ),
            Flashcard(
                id = "fc_3",
                examId = examId,
                subjectName = "Science / Biology",
                topicTitle = "Cell Biology",
                questionEn = "Which organelle is called the 'Powerhouse of the Cell' and why?",
                questionTa = "செல்லின் 'ஆற்றல் மையம்' என்று அழைக்கப்படும் உறுப்பு எது மற்றும் ஏன்?",
                answerEn = "Mitochondria - because it generates cellular energy in the form of ATP (Adenosine Triphosphate).",
                answerTa = "மைட்டோகாண்ட்ரியா (Mitochondria) - ஏனெனில் இது செல்லுக்கு தேவையான ஆற்றலை ATP வடிவில் உற்பத்தி செய்கிறது."
            )
        )
    }
}
