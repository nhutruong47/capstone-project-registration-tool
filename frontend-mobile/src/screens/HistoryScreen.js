import React from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity } from 'react-native';
import { useNavigation } from '@react-navigation/native';

const SEMESTER_DATA = [
    {
        name: 'Summer',
        stats: { total: 90, approved: 75, pending: 10, rejected: 5 }
    },
    {
        name: 'Fall',
        stats: { total: 156, approved: 130, pending: 20, rejected: 6 }
    }
];

const StatBlock = ({ label, value, color }) => (
    <View style={styles.statBlock}>
        <Text style={[styles.statValue, { color }]}>{value}</Text>
        <Text style={styles.statLabel}>{label}</Text>
    </View>
);

const SemesterCard = ({ data, style, onPress }) => {
    return (
        <TouchableOpacity style={[styles.card, style]} onPress={onPress} activeOpacity={0.8}>
            <Text style={styles.cardHeader}>{data.name}</Text>
            <View style={styles.statGrid}>
                <StatBlock label="Tổng đề tài" value={data.stats.total} color="#333" />
                <StatBlock label="Đã duyệt" value={data.stats.approved} color="#0054a4" />
                <StatBlock label="Chờ duyệt" value={data.stats.pending} color="#FFC107" />
                <StatBlock label="Từ chối" value={data.stats.rejected} color="#F26522" />
            </View>
        </TouchableOpacity>
    );
};

export default function HistoryScreen() {
    const navigation = useNavigation();

    return (
        <ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
            <Text style={styles.pageTitle}>Lịch sử các kỳ trước</Text>
            {SEMESTER_DATA.map((semester, index) => (
                <SemesterCard
                    key={index}
                    data={semester}
                    onPress={() => navigation.navigate('Manage Proposals', { term: semester.name })}
                />
            ))}
        </ScrollView>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#F5F5F5', // Light background
    },
    contentContainer: {
        padding: 16,
        paddingBottom: 40,
    },
    pageTitle: {
        fontSize: 24,
        fontWeight: 'bold',
        color: '#F26522', // FPT Orange
        marginBottom: 20,
        marginTop: 10,
    },
    // Card Styles
    card: {
        backgroundColor: '#FFFFFF',
        borderRadius: 20,
        padding: 20,
        marginBottom: 16,
        elevation: 4,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.1,
        shadowRadius: 4,
    },
    cardHeader: {
        color: '#004070', // Navy Blue
        fontSize: 22,
        fontWeight: 'bold',
        marginBottom: 16,
        borderBottomWidth: 1,
        borderBottomColor: '#EEE',
        paddingBottom: 8,
    },
    statGrid: {
        flexDirection: 'row',
        flexWrap: 'wrap',
        justifyContent: 'space-between',
    },
    statBlock: {
        width: '48%',
        marginBottom: 16,
    },
    statValue: {
        fontSize: 24,
        fontWeight: 'bold',
        marginBottom: 4,
    },
    statLabel: {
        color: '#888',
        fontSize: 14,
    },
});
