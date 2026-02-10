import { NavigationContainer } from '@react-navigation/native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { SafeAreaProvider } from 'react-native-safe-area-context';

import DashboardScreen from './src/screens/DashboardScreen';
import ManageProposalsScreen from './src/screens/ManageProposalsScreen';
import SettingsScreen from './src/screens/SettingsScreen';

import { Ionicons } from '@expo/vector-icons';
import ThesisListScreen from './src/screens/ThesisListScreen';
import HistoryScreen from './src/screens/HistoryScreen';

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
            tabBarActiveTintColor: '#fff',
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
              } else if (route.name === 'History') {
                iconName = focused ? 'time' : 'time-outline';
              }

              return <Ionicons name={iconName} size={size} color={color} />;
            },
            tabBarActiveTintColor: 'white',
            tabBarInactiveTintColor: 'gray',
            tabBarStyle: { backgroundColor: '#121212', borderTopColor: '#333' },
            headerStyle: { backgroundColor: '#121212' },
            headerTintColor: '#fff',
            headerShown: true,
          })}
        >
          <Tab.Screen name="Dashboard" component={DashboardScreen} />
          <Tab.Screen name="Manage Proposals" component={ManageProposalsScreen} />
          <Tab.Screen name="Theses" component={ThesisListScreen} />
          <Tab.Screen name="History" component={HistoryScreen} />
          <Tab.Screen name="Settings" component={SettingsScreen} />
        </Tab.Navigator>
      </NavigationContainer>
    </SafeAreaProvider>
  );
}
