import React, { useState } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, Alert } from 'react-native';

const MOCK_DATA = [
    { id: '1', title: 'Hệ thống quản lý thư viện', student: 'Nguyễn Văn A', status: 'Pending' },
    { id: '2', title: 'Ứng dụng đặt món ăn', student: 'Trần Thị B', status: 'Pending' },
    { id: '3', title: 'Website bán hàng online', student: 'Lê Văn C', status: 'Approved' },
    { id: '4', title: 'Chatbot hỗ trợ khách hàng', student: 'Phạm Thị D', status: 'Rejected' },
    { id: '5', title: 'Hệ thống điểm danh AI', student: 'Hoàng Văn E', status: 'Pending' },
];

export default function ManageProposalsScreen() {
    const [proposals, setProposals] = useState(MOCK_DATA);

    const handleApprove = (item) => {
        Alert.alert("Thành công", `Đã duyệt đề tài: ${item.title}`);
        // In a real app, you would update the status here
    };

    const getStatusColor = (status) => {
        switch (status) {
            case 'Approved': return '#4CAF50';
            case 'Rejected': return '#F44336';
            default: return '#FFC107'; // Pending
        }
    };

    const renderItem = ({ item }) => (
        <View style={styles.card}>
            <View style={styles.cardHeader}>
                <View style={{ flex: 1 }}>
                    <Text style={styles.title}>{item.title}</Text>
                    <Text style={styles.student}>{item.student}</Text>
                </View>
                <View style={[styles.badge, { backgroundColor: getStatusColor(item.status) + '20' }]}>
                    <Text style={[styles.badgeText, { color: getStatusColor(item.status) }]}>{item.status}</Text>
                </View>
            </View>

            <TouchableOpacity style={styles.button} onPress={() => handleApprove(item)}>
                <Text style={styles.buttonText}>Duyệt đề tài</Text>
            </TouchableOpacity>
        </View>
    );

    return (
        <View style={styles.container}>
            <FlatList
                data={proposals}
                keyExtractor={item => item.id}
                renderItem={renderItem}
                contentContainerStyle={styles.listContent}
            />
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#121212',
    },
    listContent: {
        padding: 16,
    },
    card: {
        backgroundColor: '#1E1E1E',
        borderRadius: 12,
        padding: 16,
        marginBottom: 16,
    },
    cardHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'flex-start',
        marginBottom: 16,
    },
    title: {
        fontSize: 16,
        fontWeight: 'bold',
        color: '#ffffff',
        marginBottom: 4,
        marginRight: 8,
    },
    student: {
        fontSize: 14,
        color: '#B0B0B0',
    },
    badge: {
        paddingHorizontal: 8,
        paddingVertical: 4,
        borderRadius: 4,
    },
    badgeText: {
        fontSize: 12,
        fontWeight: 'bold',
    },
    button: {
        backgroundColor: '#6200EE',
        paddingVertical: 10,
        borderRadius: 8,
        alignItems: 'center',
    },
    buttonText: {
        color: '#ffffff',
        fontWeight: 'bold',
        fontSize: 14,
    }
});
