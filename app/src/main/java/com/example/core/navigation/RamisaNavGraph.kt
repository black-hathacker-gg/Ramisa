package com.example.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import com.example.ui.components.GlobalSmsConfirmationBanner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

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
import com.example.features.battery.BatteryPingScreen
import com.example.features.map.OfflineMapsScreen
import com.example.features.map.OfflineMapsViewModel
import com.example.features.battery.BatteryPingViewModel
import com.example.features.history.HistoryViewModel
import com.example.features.home.HomeScreen
import com.example.features.home.HomeViewModel
import com.example.features.community.CommunitySafetyScreen
import com.example.features.community.CommunityViewModel
import com.example.features.hardware.HardwareTriggerScreen
import com.example.features.hardware.HardwareTriggerViewModel
import com.example.features.mesh.MeshNetworkScreen
import com.example.features.mesh.MeshNetworkViewModel
import com.example.features.stealth.StealthModeScreen
import com.example.features.stealth.StealthModeViewModel
import com.example.features.siren.SirenStrobeScreen
import com.example.features.siren.SirenStrobeViewModel
import com.example.features.ridesafety.RideSafetyScreen
import com.example.features.ridesafety.RideSafetyViewModel
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
import com.example.features.lockscreen.LockScreenSosScreen
import com.example.features.lockscreen.LockScreenSosViewModel
import com.example.features.map.DangerMapScreen
import com.example.features.map.DangerMapViewModel
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
import com.example.features.toolshub.ToolsHubScreen
import com.example.ui.theme.*

@Composable
fun RamisaNavGraph(
  navController: NavHostController,
  modifier: Modifier = Modifier,
  startDestination: String = Screen.Welcome.route
) {
  val emergencyViewModel: EmergencyViewModel = viewModel()
  val authViewModel: AuthViewModel = viewModel()


  val currentBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = currentBackStackEntry?.destination?.route

  val showBottomBar = currentRoute == Screen.Home.route || 
                      currentRoute == Screen.SafeJourney.route || 
                      currentRoute == Screen.DangerMap.route || 
                      currentRoute == Screen.Settings.route

  Scaffold(
    modifier = modifier,
    floatingActionButton = {
      if (showBottomBar) {
        androidx.compose.material3.FloatingActionButton(
          onClick = { navController.navigate(Screen.Emergency.route) },
          containerColor = PinkPrimary,
          contentColor = Color.White,
          shape = androidx.compose.foundation.shape.CircleShape,
          modifier = Modifier.size(64.dp)
        ) {
          Text(
            text = "SOS",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
          )
        }
      }
    },
    floatingActionButtonPosition = androidx.compose.material3.FabPosition.Center,
    bottomBar = {
      if (showBottomBar) {
        NavigationBar(
          containerColor = MaterialTheme.colorScheme.surface,
          tonalElevation = 8.dp
        ) {
          NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", style = MaterialTheme.typography.labelSmall) },
            selected = currentRoute == Screen.Home.route,
            onClick = {
              if (currentRoute != Screen.Home.route) {
                navController.navigate(Screen.Home.route) {
                  popUpTo(Screen.Home.route) { saveState = true }
                  launchSingleTop = true
                  restoreState = true
                }
              }
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = PinkPrimary,
              selectedTextColor = PinkPrimary,
              indicatorColor = PinkContainer
            )
          )

          NavigationBarItem(
            icon = { Icon(androidx.compose.material.icons.Icons.Default.DirectionsWalk, contentDescription = "Journey") },
            label = { Text("Journey", style = MaterialTheme.typography.labelSmall) },
            selected = currentRoute == Screen.SafeJourney.route,
            onClick = {
              if (currentRoute != Screen.SafeJourney.route) {
                navController.navigate(Screen.SafeJourney.route) {
                  popUpTo(Screen.Home.route) { saveState = true }
                  launchSingleTop = true
                  restoreState = true
                }
              }
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = PinkPrimary,
              selectedTextColor = PinkPrimary,
              indicatorColor = PinkContainer
            )
          )

          // Placeholder for the center SOS button to keep spacing
          NavigationBarItem(
            icon = { Spacer(modifier = Modifier.size(24.dp)) },
            label = { },
            selected = false,
            onClick = { },
            enabled = false,
            colors = NavigationBarItemDefaults.colors(
              disabledIconColor = Color.Transparent,
              disabledTextColor = Color.Transparent
            )
          )

          NavigationBarItem(
            icon = { Icon(androidx.compose.material.icons.Icons.Default.Map, contentDescription = "Map") },
            label = { Text("Map", style = MaterialTheme.typography.labelSmall) },
            selected = currentRoute == Screen.DangerMap.route,
            onClick = {
              if (currentRoute != Screen.DangerMap.route) {
                navController.navigate(Screen.DangerMap.route) {
                  popUpTo(Screen.Home.route) { saveState = true }
                  launchSingleTop = true
                  restoreState = true
                }
              }
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = PinkPrimary,
              selectedTextColor = PinkPrimary,
              indicatorColor = PinkContainer
            )
          )

          NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "You") },
            label = { Text("You", style = MaterialTheme.typography.labelSmall) },
            selected = currentRoute == Screen.Settings.route,
            onClick = {
              if (currentRoute != Screen.Settings.route) {
                navController.navigate(Screen.Settings.route) {
                  popUpTo(Screen.Home.route) { saveState = true }
                  launchSingleTop = true
                  restoreState = true
                }
              }
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = PinkPrimary,
              selectedTextColor = PinkPrimary,
              indicatorColor = PinkContainer
            )
          )
        }
      }
    }
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(innerPadding)
      ) {
      // 0. Welcome Landing Screen
      composable(Screen.Welcome.route) {
        WelcomeScreen(
          onNavigateToLogin = { navController.navigate(Screen.Login.route) },
          onNavigateToRegister = { navController.navigate(Screen.Register.route) },
          onContinueAsGuest = {
            authViewModel.loginAsGuest {
              navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Welcome.route) { inclusive = true }
              }
            }
          }
        )
      }

      // 1. Home Dashboard
      composable(Screen.Home.route) {
        HomeScreen(
          viewModel = androidx.lifecycle.viewmodel.compose.viewModel<HomeViewModel>(),
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
          onNavigateToStealthMode = { navController.navigate(Screen.StealthMode.route) },
          onNavigateToSirenStrobe = { navController.navigate(Screen.SirenStrobe.route) },
          onNavigateToRideSafety = { navController.navigate(Screen.RideSafety.route) },
          onNavigateToSelfDefense = { navController.navigate(Screen.SelfDefense.route) },
          onNavigateToCyberCrimeSupport = { navController.navigate(Screen.CyberCrimeSupport.route) },
          onNavigateToWellness = { navController.navigate(Screen.WellnessCounseling.route) },
          onNavigateToVolunteerEscort = { navController.navigate(Screen.VolunteerEscort.route) },
          onNavigateToLegalAid = { navController.navigate(Screen.LegalAid.route) },
          onNavigateToSafetyTimer = { navController.navigate(Screen.SafetyTimer.route) },
          onNavigateToFirstAid = { navController.navigate(Screen.FirstAid.route) },
          onNavigateToLockScreenSos = { navController.navigate(Screen.LockScreenSos.route) },
          onNavigateToDangerMap = { navController.navigate(Screen.DangerMap.route) },
          onTriggerSos = {
            emergencyViewModel.triggerSos()
            navController.navigate(Screen.Emergency.route)
          }
        )
      }

      // 2. Login Screen
      composable(Screen.Login.route) {
        LoginScreen(
          viewModel = androidx.lifecycle.viewmodel.compose.viewModel<AuthViewModel>(),
          onLoginSuccess = {
            navController.navigate(Screen.Home.route) {
              popUpTo(Screen.Login.route) { inclusive = true }
            }
          },
          onNavigateToRegister = { navController.navigate(Screen.Register.route) },
          onContinueAsGuest = {
            authViewModel.loginAsGuest {
              navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
              }
            }
          }
        )
      }

      // 3. Register Screen
      composable(Screen.Register.route) {
        RegisterScreen(
          viewModel = androidx.lifecycle.viewmodel.compose.viewModel<AuthViewModel>(),
          onRegisterSuccess = {
            navController.navigate(Screen.Home.route) {
              popUpTo(Screen.Register.route) { inclusive = true }
            }
          },
          onNavigateBack = { navController.popBackStack() }
        )
      }

      // 4. User Profile Screen
      composable(Screen.Profile.route) {
        ProfileScreen(
          viewModel = androidx.lifecycle.viewmodel.compose.viewModel<ProfileViewModel>(),
          onNavigateBack = { navController.popBackStack() }
        )
      }

      // 5. Emergency Contacts Screen
      composable(Screen.EmergencyContacts.route) {
        EmergencyContactsScreen(
          viewModel = androidx.lifecycle.viewmodel.compose.viewModel<ContactsViewModel>(),
          onNavigateBack = { navController.popBackStack() }
        )
      }

      // 6. Safe Journey Live Tracking
      composable(Screen.SafeJourney.route) {
        SafeJourneyScreen(
          viewModel = androidx.lifecycle.viewmodel.compose.viewModel<JourneyViewModel>(),
          onNavigateBack = { navController.popBackStack() },
          onTriggerSos = {
            emergencyViewModel.triggerSos()
            navController.navigate(Screen.Emergency.route)
          }
        )
      }

      // 7. Active SOS Emergency Mode
      composable(Screen.Emergency.route) {
        EmergencyScreen(
          viewModel = androidx.lifecycle.viewmodel.compose.viewModel<EmergencyViewModel>(),
          onResolveEmergency = { navController.popBackStack() }
        )
      }

      // 8. Incident History & Logs
      composable(Screen.History.route) {
        HistoryScreen(
          viewModel = androidx.lifecycle.viewmodel.compose.viewModel<HistoryViewModel>(),
          onNavigateBack = { navController.popBackStack() }
        )
      }

      // 9. Settings Screen
      composable(Screen.Settings.route) {
        SettingsScreen(
          viewModel = androidx.lifecycle.viewmodel.compose.viewModel<SettingsViewModel>(),
          onNavigateBack = { navController.popBackStack() },
          onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
          onLogout = {
            authViewModel.logout {
              navController.navigate(Screen.Welcome.route) {
                popUpTo(0) { inclusive = true }
              }
            }
          }
        )
      }


      // 11. Safe Places Radar
      composable(Screen.SafePlaces.route) {
        SafePlacesScreen(
          viewModel = androidx.lifecycle.viewmodel.compose.viewModel<SafePlacesViewModel>(),
          onNavigateBack = { navController.popBackStack() }
        )
      }

      // 12. Fake Escape Call
      composable(Screen.FakeCall.route) {
        FakeCallScreen(
          viewModel = androidx.lifecycle.viewmodel.compose.viewModel<FakeCallViewModel>(),
          onNavigateBack = { navController.popBackStack() }
        )
      }

      // 13. Safety Guide & Rights
      composable(Screen.SafetyGuide.route) {
        SafetyGuideScreen(
          viewModel = androidx.lifecycle.viewmodel.compose.viewModel<SafetyGuideViewModel>(),
          onNavigateBack = { navController.popBackStack() }
        )
      }

      // 14. AI Threat Guard
      composable(Screen.ThreatGuard.route) {
        ThreatGuardScreen(
          viewModel = androidx.lifecycle.viewmodel.compose.viewModel<ThreatGuardViewModel>(),
          onNavigateBack = { navController.popBackStack() },
          onTriggerSos = {
            emergencyViewModel.triggerSos()
            navController.navigate(Screen.Emergency.route)
          }
        )
      }

      // 15. Community Safety Zones
      composable(Screen.CommunitySafety.route) {
        CommunitySafetyScreen(
          viewModel = androidx.lifecycle.viewmodel.compose.viewModel<CommunityViewModel>(),
          onNavigateBack = { navController.popBackStack() }
        )
      }

      // 16. Hardware Trigger Setup
      composable(Screen.HardwareTrigger.route) {
        HardwareTriggerScreen(
          viewModel = androidx.lifecycle.viewmodel.compose.viewModel<HardwareTriggerViewModel>(),
          onNavigateBack = { navController.popBackStack() },
          onTriggerSos = {
            emergencyViewModel.triggerSos()
            navController.navigate(Screen.Emergency.route)
          }
        )
      }

      // 17. Offline Mesh Network
      composable(Screen.MeshNetwork.route) {
        MeshNetworkScreen(
          viewModel = androidx.lifecycle.viewmodel.compose.viewModel<MeshNetworkViewModel>(),
          onNavigateBack = { navController.popBackStack() }
        )
      }


      // 19. Stealth Calculator Mode
      composable(Screen.StealthMode.route) {
        StealthModeScreen(
          viewModel = androidx.lifecycle.viewmodel.compose.viewModel<StealthModeViewModel>(),
          onTriggerSilentSos = {
            emergencyViewModel.triggerSos()
            navController.navigate(Screen.Emergency.route)
          },
          onExitStealthToHome = {
            navController.navigate(Screen.Home.route) {
              popUpTo(Screen.Home.route) { inclusive = true }
            }
          }
        )
      }

      // 20. Siren & Strobe Alarm
      composable(Screen.SirenStrobe.route) {
        SirenStrobeScreen(
          viewModel = androidx.lifecycle.viewmodel.compose.viewModel<SirenStrobeViewModel>(),
          onNavigateBack = { navController.popBackStack() }
        )
      }

      // 21. Rickshaw & Ride Safety
      composable(Screen.RideSafety.route) {
        RideSafetyScreen(
          viewModel = androidx.lifecycle.viewmodel.compose.viewModel<RideSafetyViewModel>(),
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
          viewModel = androidx.lifecycle.viewmodel.compose.viewModel<SelfDefenseViewModel>(),
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
          viewModel = androidx.lifecycle.viewmodel.compose.viewModel<CyberCrimeViewModel>(),
          onNavigateBack = { navController.popBackStack() }
        )
      }

      // 25. Trauma Recovery & Counseling
      composable(Screen.WellnessCounseling.route) {
        WellnessCounselingScreen(
          viewModel = androidx.lifecycle.viewmodel.compose.viewModel<WellnessViewModel>(),
          onNavigateBack = { navController.popBackStack() }
        )
      }

      // 26. Volunteer Peer Escort Network
      composable(Screen.VolunteerEscort.route) {
        VolunteerEscortScreen(
          viewModel = androidx.lifecycle.viewmodel.compose.viewModel<VolunteerEscortViewModel>(),
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
          viewModel = androidx.lifecycle.viewmodel.compose.viewModel<LegalAidViewModel>(),
          onNavigateBack = { navController.popBackStack() }
        )
      }

      // 28. Safety Check-in Timer (Dead Man's Switch)
      composable(Screen.SafetyTimer.route) {
        SafetyTimerScreen(
          viewModel = androidx.lifecycle.viewmodel.compose.viewModel<SafetyTimerViewModel>(),
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
          viewModel = androidx.lifecycle.viewmodel.compose.viewModel<FirstAidViewModel>(),
          onNavigateBack = { navController.popBackStack() }
        )
      }



      // 31. Lock Screen & Offline SOS Guard
      composable(Screen.LockScreenSos.route) {
        LockScreenSosScreen(
          viewModel = androidx.lifecycle.viewmodel.compose.viewModel<LockScreenSosViewModel>(),
          onNavigateBack = { navController.popBackStack() },
          onTriggerSos = {
            emergencyViewModel.triggerSos()
            navController.navigate(Screen.Emergency.route)
          }
        )
      }

      // 32. Danger & Sexual Harassment Incident Heatmap
      composable(Screen.DangerMap.route) {
        DangerMapScreen(
          viewModel = androidx.lifecycle.viewmodel.compose.viewModel<DangerMapViewModel>(),
          onNavigateBack = { navController.popBackStack() },
          onTriggerSos = {
            emergencyViewModel.triggerSos()
            navController.navigate(Screen.Emergency.route)
          }
        )
      }

      // 34. Low-Battery Auto-Ping
      composable(Screen.BatteryPing.route) {
        BatteryPingScreen(
          viewModel = androidx.lifecycle.viewmodel.compose.viewModel<BatteryPingViewModel>(),
          onNavigateBack = { navController.popBackStack() }
        )
      }
      // 35. Offline Maps
      composable(Screen.OfflineMaps.route) {
        OfflineMapsScreen(
          viewModel = androidx.lifecycle.viewmodel.compose.viewModel<OfflineMapsViewModel>(),
          onNavigateBack = { navController.popBackStack() }
        )
      }
      // 33. Tools Hub Screen
      composable(Screen.ToolsHub.route) {
        ToolsHubScreen(
          onNavigateToSafetyGuide = { navController.navigate(Screen.SafetyGuide.route) },
          onNavigateToCyberCrimeSupport = { navController.navigate(Screen.CyberCrimeSupport.route) },
          onNavigateToLegalAid = { navController.navigate(Screen.LegalAid.route) },
          onNavigateToFirstAid = { navController.navigate(Screen.FirstAid.route) },
          onNavigateToWellness = { navController.navigate(Screen.WellnessCounseling.route) },
          onNavigateToSelfDefense = { navController.navigate(Screen.SelfDefense.route) },
          onNavigateToSafetyTimer = { navController.navigate(Screen.SafetyTimer.route) },
          onNavigateToSafePlaces = { navController.navigate(Screen.SafePlaces.route) },
          onNavigateToThreatGuard = { navController.navigate(Screen.ThreatGuard.route) },
          onNavigateToRideSafety = { navController.navigate(Screen.RideSafety.route) },
          onNavigateToMeshNetwork = { navController.navigate(Screen.MeshNetwork.route) },
          onNavigateToHardwareTrigger = { navController.navigate(Screen.HardwareTrigger.route) },
          onNavigateToStealthMode = { navController.navigate(Screen.StealthMode.route) },
          onNavigateToHistory = { navController.navigate(Screen.History.route) },
          onNavigateToBatteryPing = { navController.navigate(Screen.BatteryPing.route) },
          onNavigateToOfflineMaps = { navController.navigate(Screen.OfflineMaps.route) },
          onNavigateToVolunteerEscort = { navController.navigate(Screen.VolunteerEscort.route) },
          onNavigateToCommunitySafety = { navController.navigate(Screen.CommunitySafety.route) },
          onNavigateToFakeCall = { navController.navigate(Screen.FakeCall.route) },
          onNavigateToSirenStrobe = { navController.navigate(Screen.SirenStrobe.route) },
          onNavigateToSafeJourney = { navController.navigate(Screen.SafeJourney.route) },
          onNavigateToContacts = { navController.navigate(Screen.EmergencyContacts.route) },
          onTriggerSos = {
            emergencyViewModel.triggerSos()
            navController.navigate(Screen.Emergency.route)
          }
        )
      }
    }

    // Top Floating Confirmation Prompt for Offline SMS Delivery
    GlobalSmsConfirmationBanner(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = innerPadding.calculateTopPadding() + 4.dp),
      isBangla = false
    )
  }
  }
}
