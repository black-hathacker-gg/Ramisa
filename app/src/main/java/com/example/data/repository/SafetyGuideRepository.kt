package com.example.data.repository

import com.example.domain.model.SafetyTip
import com.example.domain.model.TipCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SafetyGuideRepository {

  private val tips = listOf(
    SafetyTip(
      id = "tip_1",
      titleEn = "Rickshaw & CNG Safety in Dhaka",
      titleBn = "রিকশা ও সিএনজি চলাচলে সতর্কতা",
      summaryEn = "Always note the registration plate and keep live tracking enabled.",
      summaryBn = "সিএনজি বা রিকশায় ওঠার আগে নম্বর প্লেট দেখে নিন এবং ট্র্যাকিং চালু রাখুন।",
      detailsEn = "Before boarding a CNG or Rickshaw, take a photo or note the Metro registration plate (e.g. ঢাকা মেট্রো-থ ১১-২২৩৩). Sit in the middle, keep your bag towards the inner side away from open grills, and always share your live location with a trusted guardian.",
      detailsBn = "সিএনজি বা রিকশায় উঠার সময় ঢাকা মেট্রোর নাম্বার প্লেট লক্ষ্য করুন। ব্যাগ সবসময় ভেতরের দিকে রাখুন যাতে বাইরে থেকে কেউ টানতে না পারে। বিশ্বস্ত কাউকে লাইভ লোকেশন শেয়ার করে রাখুন।",
      category = TipCategory.TRANSIT
    ),
    SafetyTip(
      id = "tip_2",
      titleEn = "Women & Child Repression Prevention Act 2000",
      titleBn = "নারী ও শিশু নির্যাতন দমন আইন ২০০০",
      summaryEn = "Know your legal protections and direct assistance mechanisms under Bangladesh law.",
      summaryBn = "বাংলাদেশ আইনের অধীনে আপনার আইনি সুরক্ষা ও তাৎক্ষণিক সহায়তার অধিকার জানুন।",
      detailsEn = "Under Section 10 and 9(ক) of the Women & Children Repression Prevention Act (Amended 2003/2020), sexual harassment, stalking, and physical abuse in public or private spaces are non-bailable criminal offenses punishable with rigorous imprisonment. Free legal aid is available via 16430 (National Legal Aid Services).",
      detailsBn = "নারী ও শিশু নির্যাতন দমন আইনের ১০ ধারা অনুযায়ী রাস্তাঘাটে বা যেকোনো স্থানে যৌন হয়রানি, ইভটিজিং বা অশালীন আচরণ জামিন অযোগ্য অপরাধ। জাতীয় আইনগত সহায়তা সংস্থার টোল-ফ্রি হটলাইন ১৬৪৩০ নম্বরে কল করে সরকারি খরচে বিনামূল্যে আইনি সহায়তা পাওয়া যায়।",
      category = TipCategory.LEGAL_RIGHTS,
      legalActRef = "Women & Children Repression Act 2000 (Act VIII of 2000)"
    ),
    SafetyTip(
      id = "tip_3",
      titleEn = "Cyberbullying & Social Media Harassment",
      titleBn = "সাইবার হয়রানি ও অনলাইন নিরাপত্তা",
      summaryEn = "How to report blackmail, impersonation, or leaked photos safely.",
      summaryBn = "ব্ল্যাকমেইল, ফেক আইডি বা ছবি ছড়ানোর বিরুদ্ধে তাৎক্ষণিক আইনি পদক্ষেপ।",
      detailsEn = "If facing cyber harassment or blackmail, do NOT delete messages or chat logs. Take clear screenshots of profiles, URLs, and timestamps. Contact the Police Cyber Support for Women (PCSW) helpline at +8801320000888 or report directly via 999.",
      detailsBn = "অনলাইনে হেনস্তা বা ব্ল্যাকমেইলের শিকার হলে মেসেজ বা প্রমাণ ডিলিট করবেন না। স্ক্রিনশট ও প্রোফাইল লিঙ্ক সংরক্ষণ করুন। বাংলাদেশ পুলিশের Police Cyber Support for Women (PCSW) হটলাইন ০১৩২-০০০০৮৮৮ অথবা ৯৯৯-এ যোগাযোগ করুন।",
      category = TipCategory.CYBER_SAFETY,
      legalActRef = "Cyber Security Act 2023 / PCSW Direct Support"
    ),
    SafetyTip(
      id = "tip_4",
      titleEn = "Evening & Night Walking Protocols",
      titleBn = "সন্ধ্যা বা রাতের বেলা একা চলাচলের নিয়ম",
      summaryEn = "Stay under streetlights, avoid earphones in isolated alleys.",
      summaryBn = "আলোকিত প্রধান সড়ক ব্যবহার করুন, নির্জন গলিতে ইয়ারফোন পরিহার করুন।",
      detailsEn = "Walk purposefully with head up and confidence. Avoid wearing noise-cancelling earphones at night. If you feel you are being followed, cross the street or enter an open commercial store or pharmacy immediately and activate RAMISA Fake Call or Safe Journey.",
      detailsBn = "রাতের বেলা হাঁটার সময় দুই কানে হেডফোন লাগিয়ে গান শোনা থেকে বিরত থাকুন। পেছনে কেউ অনুসরণ করছে সন্দেহ হলে তাৎক্ষণিক রাস্তা পার হয়ে কোনো খোলা দোকান বা ফার্মেসিতে আশ্রয় নিন এবং অ্যাপে ফেক কল বা সেফ ট্রিপ চালু করুন।",
      category = TipCategory.STREET_SAFETY
    ),
    SafetyTip(
      id = "tip_5",
      titleEn = "Medical Emergency & Immediate First Aid",
      titleBn = "জরুরি প্রাথমিক চিকিৎসা ও সহায়তা",
      summaryEn = "Basic trauma, fainting, and emergency hospital admittance procedures.",
      summaryBn = "অজ্ঞান হওয়া, রক্তক্ষরণ বা আঘাত পেলে দ্রুত করণীয়।",
      detailsEn = "In case of severe panic or fainting, elevate legs, loosen tight clothing, and ensure good ventilation. Dial 999 for ambulance dispatch or visit the nearest government hospital One-Stop Crisis Centre (OCC).",
      detailsBn = "আতঙ্ক বা অজ্ঞান হলে রোগীকে শুইয়ে পা কিছুটা উঁচুতে রাখুন এবং পর্যাপ্ত বাতাস নিশ্চিত করুন। জরুরি অ্যাম্বুলেন্সের জন্য ৯৯৯ অথবা নিকটস্থ সরকারি মেডিকেল কলেজের ওয়ান-স্টপ ক্রাইসিস সেন্টারে (OCC) যোগাযোগ করুন।",
      category = TipCategory.FIRST_AID
    )
  )

  private val _tipsFlow = MutableStateFlow(tips)
  val tipsFlow: StateFlow<List<SafetyTip>> = _tipsFlow.asStateFlow()

  fun getTipsByCategory(category: TipCategory?): List<SafetyTip> {
    return if (category == null) tips else tips.filter { it.category == category }
  }
}
