package com.example.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.features.auth.AuthViewModel
import com.example.features.auth.LoginScreen
import com.example.features.auth.RegisterScreen
import com.example.features.auth.WelcomeScreen
import com.example.features.contacts.ContactsViewModel
import com.example.features.contacts.EmergencyContactsScreen
import com.example.features.emergency.EmergencyScreen
import com.example.features.emergency.EmergencyViewModel
import com.example.features.fakecall.FakeCallScreen
import com.example.features.fakecall.FakeCallViewModel
import com.example.features.guide.SafetyGuideScreen
import com.example.features.guide.SafetyGuideViewModel
import com.example.features.history.HistoryScreen
import com.example.features.history.HistoryViewModel
import com.example.features.home.HomeScreen
import com.example.features.home.HomeViewModel
import com.example.features.community.CommunitySafetyScreen
import com.example.features.community.CommunityViewModel
import com.example.features.hardware.HardwareTriggerScreen
import com.example.features.hardware.HardwareTriggerViewModel
import com.example.features.mesh.MeshNetworkScreen
import com.example.features.mesh.MeshNetworkViewModel
import com.example.features.circle.GuardianCircleScreen
import com.example.features.circle.GuardianCircleViewModel
import com.example.features.stealth.StealthModeScreen
import com.example.features.stealth.StealthModeViewModel
import com.example.features.siren.SirenStrobeScreen
import com.example.features.siren.SirenStrobeViewModel
import com.example.features.ridesafety.RideSafetyScreen
import com.example.features.ridesafety.RideSafetyViewModel
import com.example.features.campus.CampusSafetyScreen
import com.example.features.campus.CampusSafetyViewModel
import com.example.features.defense.SelfDefenseScreen
import com.example.features.defense.SelfDefenseViewModel
import com.example.features.cyber.CyberCrimeSupportScreen
import com.example.features.cyber.CyberCrimeViewModel
import com.example.features.wellness.WellnessCounselingScreen
import com.example.features.wellness.WellnessViewModel
import com.example.features.volunteer.VolunteerEscortScreen
import com.example.features.volunteer.VolunteerEscortViewModel
import com.example.features.legalaid.LegalAidScreen
import com.example.features.legalaid.LegalAidViewModel
import com.example.features.timer.SafetyTimerScreen
import com.example.features.timer.SafetyTimerViewModel
import com.example.features.firstaid.FirstAidScreen
import com.example.features.firstaid.FirstAidViewModel
import com.example.features.child.ChildSafetyScreen
import com.example.features.child.ChildSafetyViewModel
import com.example.features.lockscreen.LockScreenSosScreen
import com.example.features.lockscreen.LockScreenSosViewModel
import com.example.features.journey.JourneyViewModel
import com.example.features.journey.SafeJourneyScreen
import com.example.features.places.SafePlacesScreen
import com.example.features.places.SafePlacesViewModel
import com.example.features.profile.ProfileScreen
import com.example.features.profile.ProfileViewModel
import com.example.features.settings.SettingsScreen
import com.example.features.settings.SettingsViewModel
import com.example.features.threat.ThreatGuardScreen
import com.example.features.threat.ThreatGuardViewModel
import com.example.features.vault.VaultScreen
import com.example.features.vault.VaultViewModel

@Composable
fun RamisaNavGraph(
  navController: NavHostController,
  modifier: Modifier = Modifier,
  startDestination: String = Screen.Home.route
) {
  val homeViewModel: HomeViewModel = viewModel()
  val authViewModel: AuthViewModel = viewModel()
  val profileViewModel: ProfileViewModel = viewModel()
  val contactsViewModel: ContactsViewModel = viewModel()
  val journeyViewModel: JourneyViewModel = viewModel()
  val emergencyViewModel: EmergencyViewModel = viewModel()
  val historyViewModel: HistoryViewModel = viewModel()
  val settingsViewModel: SettingsViewModel = viewModel()
  val vaultViewModel: VaultViewModel = viewModel()
  val safePlacesViewModel: SafePlacesViewModel = viewModel()
  val fakeCallViewModel: FakeCallViewModel = viewModel()
  val safetyGuideViewModel: SafetyGuideViewModel = viewModel()
  val threatGuardViewModel: ThreatGuardViewModel = viewModel()
  val communityViewModel: CommunityViewModel = viewModel()
  val hardwareTriggerViewModel: HardwareTriggerViewModel = viewModel()
  val meshNetworkViewModel: MeshNetworkViewModel = viewModel()
  val guardianCircleViewModel: GuardianCircleViewModel = viewModel()
  val stealthModeViewModel: StealthModeViewModel = viewModel()
  val sirenStrobeViewModel: SirenStrobeViewModel = viewModel()
  val rideSafetyViewModel: RideSafetyViewModel = viewModel()
  val campusSafetyViewModel: CampusSafetyViewModel = viewModel()
  val selfDefenseViewModel: SelfDefenseViewModel = viewModel()
  val cyberCrimeViewModel: CyberCrimeViewModel = viewModel()
  val wellnessViewModel: WellnessViewModel = viewModel()
  val volunteerEscortViewModel: VolunteerEscortViewModel = viewModel()
  val legalAidViewModel: LegalAidViewModel = viewModel()
  val safetyTimerViewModel: SafetyTimerViewModel = viewModel()
  val firstAidViewModel: FirstAidViewModel = viewModel()
  val childSafetyViewModel: ChildSafetyViewModel = viewModel()
  val lockScreenSosViewModel: LockScreenSosViewModel = viewModel()

  NavHost(
    navController = navController,
    startDestination = startDestination,
    modifier = modifier
  ) {
    // 0. Welcome Landing Screen
    composable(Screen.Welcome.route) {
      WelcomeScreen(
        onNavigateToLogin = { navController.navigate(Screen.Login.route) },
        onNavigateToRegister = { navController.navigate(Screen.Register.route) }
      )
    }

    // 1. Home Dashboard
    composable(Screen.Home.route) {
      HomeScreen(
        viewModel = homeViewModel,
        onNavigateToSafeJourney = { navController.navigate(Screen.SafeJourney.route) },
        onNavigateToContacts = { navController.navigate(Screen.EmergencyContacts.route) },
        onNavigateToHistory = { navController.navigate(Screen.History.route) },
        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
        onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
        onNavigateToSafePlaces = { navController.navigate(Screen.SafePlaces.route) },
        onNavigateToFakeCall = { navController.navigate(Screen.FakeCall.route) },
        onNavigateToSafetyGuide = { navController.navigate(Screen.SafetyGuide.route) },
        onNavigateToThreatGuard = { navController.navigate(Screen.ThreatGuard.route) },
        onNavigateToCommunitySafety = { navController.navigate(Screen.CommunitySafety.route) },
        onNavigateToHardwareTrigger = { navController.navigate(Screen.HardwareTrigger.route) },
        onNavigateToMeshNetwork = { navController.navigate(Screen.MeshNetwork.route) },
        onNavigateToVault = { navController.navigate(Screen.Vault.route) },
        onNavigateToGuardianCircle = { navController.navigate(Screen.GuardianCircle.route) },
        onNavigateToStealthMode = { navController.navigate(Screen.StealthMode.route) },
        onNavigateToSirenStrobe = { navController.navigate(Screen.SirenStrobe.route) },
        onNavigateToRideSafety = { navController.navigate(Screen.RideSafety.route) },
        onNavigateToCampusSafety = { navController.navigate(Screen.CampusSafety.route) },
        onNavigateToSelfDefense = { navController.navigate(Screen.SelfDefense.route) },
        onNavigateToCyberCrimeSupport = { navController.navigate(Screen.CyberCrimeSupport.route) },
        onNavigateToWellness = { navController.navigate(Screen.WellnessCounseling.route) },
        onNavigateToVolunteerEscort = { navController.navigate(Screen.VolunteerEscort.route) },
        onNavigateToLegalAid = { navController.navigate(Screen.LegalAid.route) },
        onNavigateToSafetyTimer = { navController.navigate(Screen.SafetyTimer.route) },
        onNavigateToFirstAid = { navController.navigate(Screen.FirstAid.route) },
        onNavigateToChildSafety = { navController.navigate(Screen.ChildSafety.route) },
        onNavigateToLockScreenSos = { navController.navigate(Screen.LockScreenSos.route) },
        onTriggerSos = {
          emergencyViewModel.triggerSos()
          navController.navigate(Screen.Emergency.route)
        }
      )
    }

    // 2. Login Screen
    composable(Screen.Login.route) {
      LoginScreen(
        viewModel = authViewModel,
        onLoginSuccess = {
          navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Login.route) { inclusive = true }
          }
        },
        onNavigateToRegister = { navController.navigate(Screen.Register.route) }
      )
    }

    // 3. Register Screen
    composable(Screen.Register.route) {
      RegisterScreen(
        viewModel = authViewModel,
        onRegisterSuccess = {
          navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Register.route) { inclusive = true }
          }
        },
        onNavigateBack = { navController.popBackStack() }
      )
    }

    // 4. Safety Profile Screen
    composable(Screen.Profile.route) {
      ProfileScreen(
        viewModel = profileViewModel,
        onNavigateBack = { navController.popBackStack() }
      )
    }

    // 5. Emergency Contacts Screen
    composable(Screen.EmergencyContacts.route) {
      EmergencyContactsScreen(
        viewModel = contactsViewModel,
        onNavigateBack = { navController.popBackStack() }
      )
    }

    // 6. Safe Journey Screen
    composable(Screen.SafeJourney.route) {
      SafeJourneyScreen(
        viewModel = journeyViewModel,
        onNavigateBack = { navController.popBackStack() },
        onTriggerSos = {
          emergencyViewModel.triggerSos()
          navController.navigate(Screen.Emergency.route)
        }
      )
    }

    // 7. Emergency SOS Active Screen
    composable(Screen.Emergency.route) {
      EmergencyScreen(
        viewModel = emergencyViewModel,
        onResolveEmergency = { navController.popBackStack() }
      )
    }

    // 8. Emergency History Screen
    composable(Screen.History.route) {
      HistoryScreen(
        viewModel = historyViewModel,
        onNavigateBack = { navController.popBackStack() }
      )
    }

    // 9. Settings Screen
    composable(Screen.Settings.route) {
      SettingsScreen(
        viewModel = settingsViewModel,
        onNavigateBack = { navController.popBackStack() },
        onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
        onNavigateToVault = { navController.navigate(Screen.Vault.route) },
        onLogout = {
          authViewModel.logout {
            navController.navigate(Screen.Welcome.route) {
              popUpTo(0) { inclusive = true }
            }
          }
        }
      )
    }

    // 10. Encrypted Safety Vault Screen
    composable(Screen.Vault.route) {
      VaultScreen(
        viewModel = vaultViewModel,
        onNavigateBack = { navController.popBackStack() }
      )
    }

    // 11. Safe Places Radar Screen
    composable(Screen.SafePlaces.route) {
      SafePlacesScreen(
        viewModel = safePlacesViewModel,
        onNavigateBack = { navController.popBackStack() }
      )
    }

    // 12. Fake Escape Call Screen
    composable(Screen.FakeCall.route) {
      FakeCallScreen(
        viewModel = fakeCallViewModel,
        onNavigateBack = { navController.popBackStack() }
      )
    }

    // 13. Safety & Legal Rights Guide
    composable(Screen.SafetyGuide.route) {
      SafetyGuideScreen(
        viewModel = safetyGuideViewModel,
        onNavigateBack = { navController.popBackStack() }
      )
    }

    // 14. AI Threat Guard & Audio Monitor
    composable(Screen.ThreatGuard.route) {
      ThreatGuardScreen(
        viewModel = threatGuardViewModel,
        onNavigateBack = { navController.popBackStack() },
        onTriggerSos = {
          emergencyViewModel.triggerSos()
          navController.navigate(Screen.Emergency.route)
        }
      )
    }

    // 15. Community Safe Zones & Live Guardian Share
    composable(Screen.CommunitySafety.route) {
      CommunitySafetyScreen(
        viewModel = communityViewModel,
        onNavigateBack = { navController.popBackStack() }
      )
    }

    // 16. Hardware & Wearable Triggers
    composable(Screen.HardwareTrigger.route) {
      HardwareTriggerScreen(
        viewModel = hardwareTriggerViewModel,
        onNavigateBack = { navController.popBackStack() },
        onTriggerSos = {
          emergencyViewModel.triggerSos()
          navController.navigate(Screen.Emergency.route)
        }
      )
    }

    // 17. Zero-Internet Offline Mesh SOS
    composable(Screen.MeshNetwork.route) {
      MeshNetworkScreen(
        viewModel = meshNetworkViewModel,
        onNavigateBack = { navController.popBackStack() }
      )
    }

    // 18. Guardian Circles Live Peer Monitor
    composable(Screen.GuardianCircle.route) {
      GuardianCircleScreen(
        viewModel = guardianCircleViewModel,
        onNavigateBack = { navController.popBackStack() }
      )
    }

    // 19. Discreet Camouflage Calculator & Stealth SOS
    composable(Screen.StealthMode.route) {
      StealthModeScreen(
        viewModel = stealthModeViewModel,
        onTriggerSilentSos = {
          emergencyViewModel.triggerSos()
        },
        onExitStealthToHome = {
          navController.popBackStack()
        }
      )
    }

    // 20. Siren & Strobe Deterrent
    composable(Screen.SirenStrobe.route) {
      SirenStrobeScreen(
        viewModel = sirenStrobeViewModel,
        onNavigateBack = { navController.popBackStack() }
      )
    }

    // 21. Rickshaw & Public Transit Guard
    composable(Screen.RideSafety.route) {
      RideSafetyScreen(
        viewModel = rideSafetyViewModel,
        onNavigateBack = { navController.popBackStack() },
        onTriggerSos = {
          emergencyViewModel.triggerSos()
          navController.navigate(Screen.Emergency.route)
        }
      )
    }

    // 22. University Campus Safe Corridors & Proctor Desk
    composable(Screen.CampusSafety.route) {
      CampusSafetyScreen(
        viewModel = campusSafetyViewModel,
        onNavigateBack = { navController.popBackStack() },
        onTriggerSos = {
          emergencyViewModel.triggerSos()
          navController.navigate(Screen.Emergency.route)
        }
      )
    }

    // 23. Practical Self Defense Techniques
    composable(Screen.SelfDefense.route) {
      SelfDefenseScreen(
        viewModel = selfDefenseViewModel,
        onNavigateBack = { navController.popBackStack() },
        onTriggerSos = {
          emergencyViewModel.triggerSos()
          navController.navigate(Screen.Emergency.route)
        }
      )
    }

    // 24. Cyber Harassment & PCSW Support
    composable(Screen.CyberCrimeSupport.route) {
      CyberCrimeSupportScreen(
        viewModel = cyberCrimeViewModel,
        onNavigateBack = { navController.popBackStack() }
      )
    }

    // 25. Trauma Recovery & Counseling
    composable(Screen.WellnessCounseling.route) {
      WellnessCounselingScreen(
        viewModel = wellnessViewModel,
        onNavigateBack = { navController.popBackStack() }
      )
    }

    // 26. Volunteer Peer Escort Network
    composable(Screen.VolunteerEscort.route) {
      VolunteerEscortScreen(
        viewModel = volunteerEscortViewModel,
        onNavigateBack = { navController.popBackStack() },
        onTriggerSos = {
          emergencyViewModel.triggerSos()
          navController.navigate(Screen.Emergency.route)
        }
      )
    }

    // 27. Free Legal Aid & GD Drafter
    composable(Screen.LegalAid.route) {
      LegalAidScreen(
        viewModel = legalAidViewModel,
        onNavigateBack = { navController.popBackStack() }
      )
    }

    // 28. Safety Check-in Timer (Dead Man's Switch)
    composable(Screen.SafetyTimer.route) {
      SafetyTimerScreen(
        viewModel = safetyTimerViewModel,
        onNavigateBack = { navController.popBackStack() },
        onTriggerSos = {
          emergencyViewModel.triggerSos()
          navController.navigate(Screen.Emergency.route)
        }
      )
    }

    // 29. Offline First Aid & Triage Guide
    composable(Screen.FirstAid.route) {
      FirstAidScreen(
        viewModel = firstAidViewModel,
        onNavigateBack = { navController.popBackStack() }
      )
    }

    // 30. Child & Minor Protection (Geofencing & 1098)
    composable(Screen.ChildSafety.route) {
      ChildSafetyScreen(
        viewModel = childSafetyViewModel,
        onNavigateBack = { navController.popBackStack() }
      )
    }

    // 31. Lock Screen & Offline SOS Guard
    composable(Screen.LockScreenSos.route) {
      LockScreenSosScreen(
        viewModel = lockScreenSosViewModel,
        onNavigateBack = { navController.popBackStack() },
        onTriggerSos = {
          emergencyViewModel.triggerSos()
          navController.navigate(Screen.Emergency.route)
        }
      )
    }
  }
}
