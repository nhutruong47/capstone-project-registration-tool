import React from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, Alert } from 'react-native';

const ADMIN_PROFILE = {
    name: 'Admin',
    role: 'Administrator',
    avatar: 'A'
};

const SEMESTER_DATA = [
    {
        name: 'Spring',
        stats: { total: 145, approved: 100, pending: 30, rejected: 15 }
    },
    {
        name: 'Summer',
        stats: { total: 90, approved: 75, pending: 10, rejected: 5 }
    },
    {
        name: 'Fall',
        stats: { total: 156, approved: 130, pending: 20, rejected: 6 }
    }
];

const QUICK_ACTIONS = [
    { id: 1, title: 'Duyệt đề tài', icon: '✅' },
    { id: 2, title: 'Tạo đợt nộp mới', icon: '📅' },
    { id: 3, title: 'Gửi email GV', icon: '📧' },
    { id: 4, title: 'Xuất báo cáo', icon: '📊' },
];

const ProfileHeader = () => (
    <View style={styles.profileContainer}>
        <View style={styles.profileInfo}>
            <View style={styles.avatar}>
                <Text style={styles.avatarText}>{ADMIN_PROFILE.avatar}</Text>
            </View>
            <View>
                <Text style={styles.profileName}>{ADMIN_PROFILE.name}</Text>
                <Text style={styles.profileRole}>{ADMIN_PROFILE.role}</Text>
            </View>
        </View>
        <TouchableOpacity onPress={() => Alert.alert('Logout', 'Logging out...')} style={styles.logoutBtn}>
            <Text style={styles.logoutText}>🚪</Text>
        </TouchableOpacity>
    </View>
);

const StatBlock = ({ label, value, color }) => (
    <View style={styles.statBlock}>
        <Text style={[styles.statValue, { color }]}>{value}</Text>
        <Text style={styles.statLabel}>{label}</Text>
    </View>
);

const SemesterCard = ({ data }) => (
    <View style={styles.card}>
        <Text style={styles.cardHeader}>{data.name}</Text>
        <View style={styles.statGrid}>
            <StatBlock label="Tổng đề tài" value={data.stats.total} color="#B0B0B0" />
            <StatBlock label="Đã duyệt" value={data.stats.approved} color="#4CAF50" />
            <StatBlock label="Chờ duyệt" value={data.stats.pending} color="#FFC107" />
            <StatBlock label="Từ chối" value={data.stats.rejected} color="#F44336" />
        </View>
    </View>
);

const QuickActionButton = ({ item }) => (
    <TouchableOpacity
        style={styles.actionBtn}
        onPress={() => Alert.alert(item.title, 'Feature coming soon!')}
    >
        <Text style={styles.actionIcon}>{item.icon}</Text>
        <Text style={styles.actionText}>{item.title}</Text>
    </TouchableOpacity>
);

export default function DashboardScreen() {
    return (
        <ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
            <ProfileHeader />

            <Text style={styles.sectionTitle}>Thống kê đợt nộp</Text>
            {SEMESTER_DATA.map((semester, index) => (
                <SemesterCard key={index} data={semester} />
            ))}

            <Text style={styles.sectionTitle}>Thao tác nhanh</Text>
            <View style={styles.actionGrid}>
                {QUICK_ACTIONS.map(action => (
                    <QuickActionButton key={action.id} item={action} />
                ))}
            </View>
        </ScrollView>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#121212',
    },
    contentContainer: {
        padding: 16,
        paddingBottom: 40,
    },
    // Profile Styles
    profileContainer: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: 24,
        marginTop: 8,
    },
    profileInfo: {
        flexDirection: 'row',
        alignItems: 'center',
    },
    avatar: {
        width: 50,
        height: 50,
        borderRadius: 25,
        backgroundColor: '#2196F3',
        justifyContent: 'center',
        alignItems: 'center',
        marginRight: 12,
    },
    avatarText: {
        color: 'white',
        fontSize: 24,
        fontWeight: 'bold',
    },
    profileName: {
        color: 'white',
        fontSize: 18,
        fontWeight: 'bold',
    },
    profileRole: {
        color: 'gray',
        fontSize: 14,
    },
    logoutBtn: {
        padding: 8,
    },
    logoutText: {
        fontSize: 24,
    },
    // Section Styles
    sectionTitle: {
        color: 'white',
        fontSize: 18,
        fontWeight: '600',
        marginBottom: 12,
        marginTop: 8,
    },
    // Card Styles
    card: {
        backgroundColor: '#1E1E1E',
        borderRadius: 12,
        padding: 16,
        marginBottom: 16,
    },
    cardHeader: {
        color: 'white',
        fontSize: 16,
        fontWeight: 'bold',
        marginBottom: 12,
        borderBottomWidth: 1,
        borderBottomColor: '#333',
        paddingBottom: 8,
    },
    statGrid: {
        flexDirection: 'row',
        flexWrap: 'wrap',
        justifyContent: 'space-between',
    },
    statBlock: {
        width: '48%',
        marginBottom: 12,
    },
    statValue: {
        fontSize: 20,
        fontWeight: 'bold',
        marginBottom: 4,
    },
    statLabel: {
        color: '#B0B0B0',
        fontSize: 12,
    },
    // Quick Action Styles
    actionGrid: {
        flexDirection: 'row',
        flexWrap: 'wrap',
        justifyContent: 'space-between',
        gap: 12,
    },
    actionBtn: {
        width: '48%',
        aspectRatio: 1.5,
        backgroundColor: '#2C2C2C',
        borderRadius: 12,
        justifyContent: 'center',
        alignItems: 'center',
        marginBottom: 12,
        borderWidth: 1,
        borderColor: '#333',
    },
    actionIcon: {
        fontSize: 32,
        marginBottom: 8,
    },
    actionText: {
        color: 'white',
        fontSize: 14,
        fontWeight: '500',
    },
});

