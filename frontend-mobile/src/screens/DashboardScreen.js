import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, Alert, Dimensions } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Ionicons } from '@expo/vector-icons';
import { PieChart } from 'react-native-chart-kit';
import Skeleton from '../components/Skeleton';

const SCREEN_WIDTH = Dimensions.get('window').width;

const ADMIN_PROFILE = {
    name: 'Admin',
    role: 'Administrator',
    avatar: 'A'
};

const CURRENT_TERM = {
    name: 'Spring',
    stats: { total: 145, approved: 100, pending: 30, rejected: 15 }
};

const QUICK_ACTIONS = [
    { id: 1, title: 'Duyệt đề tài', icon: '✅' },
    { id: 2, title: 'Tạo đợt nộp', icon: '📅' },
    { id: 3, title: 'Gửi email GV', icon: '📧' },
    { id: 4, title: 'Xuất báo cáo', icon: '📊' },
];

const ProfileHeader = () => (
    <LinearGradient
        colors={['#F26522', '#D84315']} // FPT Orange Gradient
        start={{ x: 0, y: 0 }}
        end={{ x: 1, y: 0 }}
        style={styles.profileContainer}
    >
        <View style={styles.profileInfo}>
            <View style={styles.avatar}>
                <Text style={styles.avatarText}>{ADMIN_PROFILE.avatar}</Text>
            </View>
            <View>
                <Text style={styles.profileName}>{ADMIN_PROFILE.name}</Text>
                <Text style={styles.profileRole}>{ADMIN_PROFILE.role}</Text>
            </View>
        </View>
        <View style={styles.headerRight}>
            <TouchableOpacity style={styles.iconBtn}>
                <Ionicons name="notifications-outline" size={28} color="#005DAA" />
                <View style={styles.badge}>
                    <Text style={styles.badgeText}>1</Text>
                </View>
            </TouchableOpacity>
            <TouchableOpacity onPress={() => Alert.alert('Logout', 'Logging out...')} style={styles.iconBtn}>
                <Ionicons name="log-out-outline" size={28} color="#005DAA" />
            </TouchableOpacity>
        </View>
    </LinearGradient>
);

const StatBlock = ({ label, value, color }) => (
    <View style={styles.statBlock}>
        <Text style={[styles.statValue, { color }]}>{value}</Text>
        <Text style={styles.statLabel}>{label}</Text>
    </View>
);

const SemesterCard = ({ data, loading }) => {
    if (loading) {
        return (
            <View style={styles.card}>
                <Skeleton width={100} height={20} style={{ marginBottom: 16 }} />
                <View style={{ flexDirection: 'row', justifyContent: 'space-between' }}>
                    <Skeleton width={60} height={50} />
                    <Skeleton width={60} height={50} />
                    <Skeleton width={60} height={50} />
                    <Skeleton width={60} height={50} />
                </View>
            </View>
        );
    }

    const chartData = [
        {
            name: 'Đã duyệt',
            population: data.stats.approved,
            color: '#005DAA', // Navy Blue
            legendFontColor: '#7F7F7F',
            legendFontSize: 12,
        },
        {
            name: 'Từ chối',
            population: data.stats.rejected,
            color: '#F26522', // Orange
            legendFontColor: '#7F7F7F',
            legendFontSize: 12,
        },
    ];

    return (
        <View style={styles.card}>
            <Text style={styles.cardHeader}>{data.name}</Text>
            <View style={styles.statsContainer}>
                <View style={styles.statGrid}>
                    <StatBlock label="Tổng" value={data.stats.total} color="#333" />
                    <StatBlock label="Duyệt" value={data.stats.approved} color="#005DAA" />
                    <StatBlock label="Chờ" value={data.stats.pending} color="#FFC107" />
                    <StatBlock label="Hủy" value={data.stats.rejected} color="#F26522" />
                </View>
                <View style={styles.chartContainer}>
                    <PieChart
                        data={chartData}
                        width={80} // Reduced diameter
                        height={80} // Reduced diameter
                        chartConfig={{
                            color: (opacity = 1) => `rgba(0, 0, 0, ${opacity})`,
                        }}
                        accessor={"population"}
                        backgroundColor={"transparent"}
                        paddingLeft={"15"}
                        center={[0, 0]}
                        absolute={false}
                        hasLegend={false}
                    />
                </View>
            </View>
        </View>
    );
};

const QuickActionButton = ({ item, loading }) => {
    if (loading) {
        return <Skeleton width={(SCREEN_WIDTH - 60) / 2} height={100} style={{ borderRadius: 15, marginBottom: 8 }} />;
    }
    return (
        <TouchableOpacity
            style={styles.actionBtn}
            onPress={() => Alert.alert(item.title, 'Feature coming soon!')}
        >
            <Text style={styles.actionIcon}>{item.icon}</Text>
            <Text style={styles.actionText}>{item.title}</Text>
        </TouchableOpacity>
    );
};

export default function DashboardScreen() {
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const timer = setTimeout(() => {
            setLoading(false);
        }, 2000);

        const notificationTimer = setTimeout(() => {
            Alert.alert("Thông báo", "Có 1 đề tài mới đang chờ bạn duyệt trong kỳ Spring!");
        }, 3000);

        return () => {
            clearTimeout(timer);
            clearTimeout(notificationTimer);
        };
    }, []);

    return (
        <ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
            <ProfileHeader />

            <View style={styles.sectionContainer}>
                <Text style={styles.sectionTitle}>Học kỳ hiện tại</Text>
                <SemesterCard data={CURRENT_TERM} loading={loading} />
            </View>

            <View style={styles.sectionContainer}>
                <Text style={styles.sectionTitle}>Thao tác nhanh</Text>
                <View style={styles.actionGrid}>
                    {QUICK_ACTIONS.map(action => (
                        <QuickActionButton key={action.id} item={action} loading={loading} />
                    ))}
                </View>
            </View>
        </ScrollView>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#F5F5F5',
    },
    contentContainer: {
        paddingBottom: 40,
    },
    sectionContainer: {
        marginBottom: 24,
        paddingHorizontal: 20, // Increased to 20
    },
    // Profile Styles
    profileContainer: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        paddingVertical: 40,
        paddingHorizontal: 20,
        marginBottom: 20,
        borderBottomLeftRadius: 30,
        borderBottomRightRadius: 30,
        elevation: 5,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.2,
        shadowRadius: 4,
    },
    profileInfo: {
        flexDirection: 'row',
        alignItems: 'center',
    },
    avatar: {
        width: 50, // Slightly smaller
        height: 50,
        borderRadius: 25,
        backgroundColor: '#FFFFFF',
        justifyContent: 'center',
        alignItems: 'center',
        marginRight: 12,
        elevation: 3,
    },
    avatarText: {
        color: '#F26522',
        fontSize: 24,
        fontWeight: 'bold',
    },
    profileName: {
        color: 'white',
        fontSize: 18, // Reduced
        fontWeight: 'bold',
    },
    profileRole: {
        color: 'rgba(255, 255, 255, 0.9)',
        fontSize: 13,
    },
    headerRight: {
        flexDirection: 'row',
        gap: 12,
    },
    iconBtn: {
        padding: 8, // Reduced padding
        backgroundColor: '#FFFFFF',
        borderRadius: 25,
        width: 44, // Reduced size
        height: 44,
        justifyContent: 'center',
        alignItems: 'center',
        elevation: 3,
        position: 'relative',
    },
    badge: {
        position: 'absolute',
        top: -2,
        right: -2,
        backgroundColor: 'red',
        borderRadius: 10,
        width: 18,
        height: 18,
        justifyContent: 'center',
        alignItems: 'center',
        borderWidth: 2,
        borderColor: '#FFF',
    },
    badgeText: {
        color: 'white',
        fontSize: 9,
        fontWeight: 'bold',
    },
    // Section Styles
    sectionTitle: {
        color: '#333',
        fontSize: 18,
        fontWeight: '700',
        marginBottom: 12,
        marginTop: 8,
    },
    // Card Styles
    card: {
        backgroundColor: '#FFFFFF',
        borderRadius: 20,
        padding: 15,
        elevation: 4,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.1,
        shadowRadius: 4,
        width: '92%',
        alignSelf: 'center',
    },
    cardHeader: {
        color: '#004070',
        fontSize: 20,
        fontWeight: 'bold',
        marginBottom: 12,
        borderBottomWidth: 1,
        borderBottomColor: '#EEE',
        paddingBottom: 8,
    },
    statsContainer: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    statGrid: {
        flex: 1,
        flexDirection: 'row',
        flexWrap: 'wrap',
        justifyContent: 'space-between',
    },
    chartContainer: {
        justifyContent: 'center',
        alignItems: 'center',
        marginLeft: 10,
    },
    statBlock: {
        width: '45%',
        marginBottom: 12,
    },
    statValue: {
        fontSize: 22, // Adjusted to 22px as requested
        fontWeight: 'bold',
        marginBottom: 2,
    },
    statLabel: {
        color: '#888',
        fontSize: 12,
    },
    // Quick Action Styles
    actionGrid: {
        flexDirection: 'row',
        flexWrap: 'wrap',
        justifyContent: 'space-between',
    },
    actionBtn: {
        width: '44%', // Adjusted to 44% as requested
        backgroundColor: '#FFFFFF',
        borderRadius: 15,
        paddingVertical: 15,
        justifyContent: 'center',
        alignItems: 'center',
        marginBottom: 12,
        elevation: 3,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 1 },
        shadowOpacity: 0.1,
        shadowRadius: 2,
    },
    actionIcon: {
        fontSize: 28,
        marginBottom: 8,
        color: '#F26522',
        width: 30, // Fixed width to ensure centering/alignment
        textAlign: 'center',
    },
    actionText: {
        color: '#333',
        fontSize: 13,
        fontWeight: '600',
        textAlign: 'center',
    },
});

