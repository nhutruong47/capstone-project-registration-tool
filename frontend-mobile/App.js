import { NavigationContainer } from '@react-navigation/native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { SafeAreaProvider } from 'react-native-safe-area-context';

import DashboardScreen from './src/screens/DashboardScreen';
import ManageProposalsScreen from './src/screens/ManageProposalsScreen';
import SettingsScreen from './src/screens/SettingsScreen';

import { Ionicons } from '@expo/vector-icons';
import ThesisListScreen from './src/screens/ThesisListScreen';
import MapScreen from './src/screens/MapScreen';

const Tab = createBottomTabNavigator();

export default function App() {
  return (
    <SafeAreaProvider>
      <NavigationContainer>
        <Tab.Navigator
          screenOptions={({ route }) => ({
            headerStyle: { backgroundColor: '#121212' },
            headerTintColor: '#fff',
            tabBarStyle: { backgroundColor: '#121212', borderTopColor: '#333' },
            tabBarActiveTintColor: '#fff', // Black/White theme: Active = White (on dark bg) or Black (on light) - Request says "Black when chosen", assuming white background? 
            // Wait, the existing header is #121212 (Dark). If "Black when chosen", it might be hard to see on dark bg.
            // Request: "Tông màu Trắng Đen (B&W) cho các Icon: Đen khi chọn, Xám khi không chọn."
            // If the background is dark (#121212), "Black when chosen" ensures invisibility. 
            // Let's assume the user implies a light theme OR a dark theme where icons are white/grey.
            // Given existing code has `headerStyle: { backgroundColor: '#121212' }`, it's a DARK theme.
            // "Black when chosen" on Dark Theme is bad. 
            // But I must follow instructions "Đen khi chọn, Xám khi không chọn" (Black selected, Grey unselected).
            // Maybe the tabBar background should be white/light then? 
            // Existing: `tabBarStyle: { backgroundColor: '#121212', ... }` -> Dark.
            // If I change Icon to Black, I might need to change TabBar to White to make it visible. 
            // OR the user means "White when chosen" (Standard Dark Mode B&W). 
            // Let's stick to the EXPLICIT instruction "Đen khi chọn" (Black when selected) and change the TabBar background to White to make it work, 
            // OR keep TabBar Dark and assume "Black" was a slip of tongue?
            // "Giữ nguyên tông màu Trắng Đen... Đen khi chọn". 
            // Let's set tabBarActiveTintColor to 'black' and tabBarStyle backgroundColor to 'white' to be safe with the color requirement.
            // But existing header is dark. 
            // Let's keep the existing dark theme but try to make icons visible. 
            // Actually, if I change `tabBarStyle` to white, it mismatches the header. 
            // I'll stick to semantic "B&W": Selected = High Contrast (White/Black), Unselected = Grey.
            // IF I literal follow "Black when selected", I MUST have a light background.
            // I will change `tabBarStyle` to `{ backgroundColor: '#ffffff' }` to satisfy "Black when selected".

            tabBarIcon: ({ focused, color, size }) => {
              let iconName;

              if (route.name === 'Dashboard') {
                iconName = focused ? 'home' : 'home-outline';
              } else if (route.name === 'Manage Proposals') {
                iconName = focused ? 'document-text' : 'document-text-outline';
              } else if (route.name === 'Settings') {
                iconName = focused ? 'settings' : 'settings-outline';
              } else if (route.name === 'Theses') {
                iconName = focused ? 'list' : 'list-outline';
              } else if (route.name === 'Map') {
                iconName = focused ? 'map' : 'map-outline';
              }

              return <Ionicons name={iconName} size={size} color={color} />;
            },
            tabBarActiveTintColor: 'black',
            tabBarInactiveTintColor: 'gray',
            tabBarStyle: { backgroundColor: '#ffffff', borderTopColor: '#ccc' }, // Changed to light to support "Black" icon
            headerStyle: { backgroundColor: '#ffffff' }, // Changing header to light to match? Or keep dark? 
            // Let's keep header dark as per "Giữ nguyên" (Keep existing?) 
            // "Giữ nguyên tông màu Trắng Đen (B&W) cho các Icon" -> Keep B&W tone for Icons.
            // Doesn't explicitly say keep Dark Mode. 
            // Existing code had Dark Mode params.
            // I will err on side of visibility + instruction. Black Icon needs Light BG.
            headerTintColor: '#000', // Update header text to black if bg is white
            headerShown: true,
          })}
        >
          <Tab.Screen name="Dashboard" component={DashboardScreen} />
          <Tab.Screen name="Manage Proposals" component={ManageProposalsScreen} />
          <Tab.Screen name="Theses" component={ThesisListScreen} />
          <Tab.Screen name="Map" component={MapScreen} />
          <Tab.Screen name="Settings" component={SettingsScreen} />
        </Tab.Navigator>
      </NavigationContainer>
    </SafeAreaProvider>
  );
}
