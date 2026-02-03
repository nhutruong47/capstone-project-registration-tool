// Test git push
import React, { useState } from 'react';
import { View, Text, FlatList, StyleSheet, TextInput, Alert, TouchableOpacity } from 'react-native';
import { GestureHandlerRootView, Swipeable } from 'react-native-gesture-handler';

// Mock Data
const initialTheses = [
    { id: '1', title: 'AI in Healthcare', student: 'Nguyen Van A', studentId: 'SV001', status: 'Pending', v1: 8, v2: 7 },
    { id: '2', title: 'Blockchain Voting', student: 'Tran Thi B', studentId: 'SV002', status: 'Approved', v1: 9, v2: 8 },
    { id: '3', title: 'IoT Smart Home', student: 'Le Van C', studentId: 'SV003', status: 'Rejected', v1: 5, v2: 4 },
    { id: '4', title: 'Big Data Analysis', student: 'Pham Thi D', studentId: 'SV004', status: 'Pending', v1: 7, v2: 7 },
    { id: '5', title: 'Cloud Computing', student: 'Hoang Van E', studentId: 'SV005', status: 'Pending', v1: 6, v2: 8 },
];

const ThesisListScreen = () => {
    const [theses, setTheses] = useState(initialTheses);
    const [searchQuery, setSearchQuery] = useState('');

    const handleSearch = (text) => {
        setSearchQuery(text);
    };

    const filteredTheses = theses.filter(
        (item) =>
            item.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
            item.studentId.toLowerCase().includes(searchQuery.toLowerCase())
    );

    const calculateScore = (v1, v2) => v1 + v2;

    const handleApprove = (item) => {
        const totalScore = calculateScore(item.v1, item.v2);
        Alert.alert(
            'Phê duyệt nhanh',
            `Điểm tổng kết (T = V1 + V2): ${item.v1} + ${item.v2} = ${totalScore}\n\nĐề tài đã được duyệt!`,
            [{ text: 'OK', onPress: () => updateStatus(item.id, 'Approved') }]
        );
    };

    const updateStatus = (id, newStatus) => {
        setTheses((prev) =>
            prev.map((item) => (item.id === id ? { ...item, status: newStatus } : item))
        );
    };

    const renderRightActions = (item) => {
        return (
            <TouchableOpacity
                style={styles.approveButton}
                onPress={() => handleApprove(item)}
            >
                <Text style={styles.approveText}>Duyệt</Text>
            </TouchableOpacity>
        );
    };

    const getStatusColor = (status) => {
        switch (status) {
            case 'Approved': return '#4CAF50';
            case 'Rejected': return '#F44336';
            default: return '#FFC107';
        }
    };

    const renderItem = ({ item }) => (
        <Swipeable renderRightActions={() => renderRightActions(item)}>
            <View style={styles.itemContainer}>
                <View style={styles.infoContainer}>
                    <Text style={styles.title}>{item.title}</Text>
                    <Text style={styles.student}>{item.student} - {item.studentId}</Text>
                </View>
                <View style={[styles.badge, { backgroundColor: getStatusColor(item.status) }]}>
                    <Text style={styles.badgeText}>{item.status}</Text>
                </View>
            </View>
        </Swipeable>
    );

    return (
        <GestureHandlerRootView style={{ flex: 1 }}>
            <View style={styles.container}>
                <TextInput
                    style={styles.searchBar}
                    placeholder="Tìm kiếm theo tên đề tài hoặc mã SV..."
                    value={searchQuery}
                    onChangeText={handleSearch}
                />
                <FlatList
                    data={filteredTheses}
                    keyExtractor={(item) => item.id}
                    renderItem={renderItem}
                    contentContainerStyle={styles.listContent}
                />
            </View>
        </GestureHandlerRootView>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#f5f5f5',
    },
    searchBar: {
        margin: 10,
        padding: 10,
        backgroundColor: '#fff',
        borderRadius: 8,
        borderWidth: 1,
        borderColor: '#ddd',
    },
    listContent: {
        paddingBottom: 20,
    },
    itemContainer: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: 15,
        backgroundColor: '#fff',
        borderBottomWidth: 1,
        borderBottomColor: '#eee',
    },
    infoContainer: {
        flex: 1,
    },
    title: {
        fontSize: 16,
        fontWeight: 'bold',
        color: '#333',
    },
    student: {
        fontSize: 14,
        color: '#666',
        marginTop: 4,
    },
    badge: {
        paddingHorizontal: 10,
        paddingVertical: 5,
        borderRadius: 12,
        marginLeft: 10,
        minWidth: 80,
        alignItems: 'center',
    },
    badgeText: {
        color: '#fff',
        fontSize: 12,
        fontWeight: 'bold',
    },
    approveButton: {
        backgroundColor: '#2196F3',
        justifyContent: 'center',
        alignItems: 'center',
        width: 80,
        height: '100%',
    },
    approveText: {
        color: '#fff',
        fontWeight: 'bold',
    },
});

export default ThesisListScreen;
