import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, Alert, TextInput, ScrollView } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import Skeleton from '../components/Skeleton';

const MOCK_DATA = [
    { id: '1', title: 'Hệ thống quản lý thư viện', student: 'Nguyễn Văn A', status: 'Pending', term: 'Spring' },
    { id: '2', title: 'Ứng dụng đặt món ăn', student: 'Trần Thị B', status: 'Pending', term: 'Spring' },
    { id: '3', title: 'Website bán hàng online', student: 'Lê Văn C', status: 'Approved', term: 'Summer' },
    { id: '4', title: 'Chatbot hỗ trợ khách hàng', student: 'Phạm Thị D', status: 'Rejected', term: 'Fall' },
    { id: '5', title: 'Hệ thống điểm danh AI', student: 'Hoàng Văn E', status: 'Pending', term: 'Summer' },
    { id: '6', title: 'Sàn thương mại điện tử', student: 'Ngô Văn F', status: 'Approved', term: 'Fall' },
    { id: '7', title: 'Game giáo dục trẻ em', student: 'Đặng Thị G', status: 'Pending', term: 'Spring' },
];

const FILTERS = ['All', 'Pending', 'Approved', 'Rejected'];

export default function ManageProposalsScreen({ route }) {
    const { term } = route.params || {};
    const [proposals, setProposals] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchText, setSearchText] = useState('');
    const [filterStatus, setFilterStatus] = useState('All');

    useEffect(() => {
        setLoading(true);
        // Simulate fetch
        const timer = setTimeout(() => {
            setProposals(MOCK_DATA);
            setLoading(false);
        }, 1000); // Faster load for UX
        return () => clearTimeout(timer);
    }, [term]); // Reload if term changes

    const handleApprove = (item) => {
        Alert.alert("Thành công", `Đã duyệt đề tài: ${item.title}`);
    };

    const getStatusStyles = (status) => {
        switch (status) {
            case 'Approved':
                return {
                    bg: '#E8F5E9', // Light Green
                    text: '#2E7D32' // Green
                };
            case 'Rejected':
                return {
                    bg: '#FFEBEE', // Light Red
                    text: '#C62828' // Red
                };
            default: // Pending
                return {
                    bg: '#FFF8E1', // Light Yellow
                    text: '#F57F17' // Dark Yellow/Orange-ish for readability
                };
        }
    };

    const renderSkeleton = () => (
        <View>
            {[1, 2, 3].map(i => (
                <View key={i} style={styles.card}>
                    <View style={{ flexDirection: 'row', justifyContent: 'space-between', marginBottom: 16 }}>
                        <View style={{ flex: 1 }}>
                            <Skeleton width={200} height={20} style={{ marginBottom: 8 }} />
                            <Skeleton width={150} height={16} />
                        </View>
                        <Skeleton width={60} height={24} style={{ borderRadius: 12 }} />
                    </View>
                    <Skeleton width={'100%'} height={40} style={{ borderRadius: 8 }} />
                </View>
            ))}
        </View>
    );

    const renderItem = ({ item }) => {
        const statusStyle = getStatusStyles(item.status);

        return (
            <View style={styles.card}>
                <View style={styles.cardHeader}>
                    <View style={{ flex: 1, marginRight: 8 }}>
                        <Text style={styles.title}>{item.title}</Text>
                        <Text style={styles.student}>Thực hiện: {item.student}</Text>
                    </View>
                    <View style={[styles.badge, { backgroundColor: statusStyle.bg }]}>
                        <Text style={[styles.badgeText, { color: statusStyle.text }]}>{item.status}</Text>
                    </View>
                </View>

                <View style={styles.actionContainer}>
                    <TouchableOpacity style={styles.button} onPress={() => handleApprove(item)}>
                        <Text style={styles.buttonText}>Duyệt đề tài</Text>
                    </TouchableOpacity>
                </View>
            </View>
        );
    };

    const filteredData = proposals.filter(item => {
        const matchesTerm = term ? item.term === term : true;
        const matchesStatus = filterStatus === 'All' || item.status === filterStatus;
        const matchesSearch = item.title.toLowerCase().includes(searchText.toLowerCase()) ||
            item.student.toLowerCase().includes(searchText.toLowerCase());
        return matchesTerm && matchesStatus && matchesSearch;
    });

    const screenTitle = term ? `Danh sách đề tài - ${term}` : 'Danh sách đề tài';

    return (
        <View style={styles.container}>
            <View style={styles.headerContainer}>
                <Text style={styles.screenTitle}>{screenTitle}</Text>

                {/* Search Bar */}
                <View style={styles.searchBar}>
                    <Ionicons name="search" size={20} color="#888" style={{ marginRight: 8 }} />
                    <TextInput
                        style={styles.searchInput}
                        placeholder="Tìm kiếm đề tài, sinh viên..."
                        placeholderTextColor="#888"
                        value={searchText}
                        onChangeText={setSearchText}
                    />
                </View>

                {/* Filter Chips */}
                <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.filterContainer}>
                    {FILTERS.map(filter => (
                        <TouchableOpacity
                            key={filter}
                            style={[
                                styles.chip,
                                filterStatus === filter && styles.chipActive
                            ]}
                            onPress={() => setFilterStatus(filter)}
                        >
                            <Text style={[
                                styles.chipText,
                                filterStatus === filter && styles.chipTextActive
                            ]}>
                                {filter === 'All' ? 'Tất cả' :
                                    filter === 'Pending' ? 'Chờ duyệt' :
                                        filter === 'Approved' ? 'Đã duyệt' : 'Từ chối'}
                            </Text>
                        </TouchableOpacity>
                    ))}
                </ScrollView>
            </View>

            {loading ? (
                <View style={styles.listContent}>
                    {renderSkeleton()}
                </View>
            ) : (
                <FlatList
                    data={filteredData}
                    keyExtractor={item => item.id}
                    renderItem={renderItem}
                    contentContainerStyle={styles.listContent}
                    ListEmptyComponent={<Text style={styles.emptyText}>Không tìm thấy đề tài nào.</Text>}
                />
            )}
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#F5F5F5', // Light background
    },
    headerContainer: {
        backgroundColor: '#FFF',
        paddingBottom: 16,
        elevation: 2,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 1 },
        shadowOpacity: 0.1,
        shadowRadius: 2,
    },
    screenTitle: {
        fontSize: 24,
        fontWeight: 'bold',
        color: '#F26522', // FPT Orange
        paddingHorizontal: 16,
        paddingTop: 16,
        paddingBottom: 12,
    },
    searchBar: {
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: '#F0F0F0',
        marginHorizontal: 16,
        paddingHorizontal: 12,
        borderRadius: 20, // Rounded
        height: 40,
        marginBottom: 12,
    },
    searchInput: {
        flex: 1,
        color: '#333',
    },
    filterContainer: {
        paddingHorizontal: 16,
    },
    chip: {
        paddingHorizontal: 16,
        paddingVertical: 6,
        borderRadius: 20,
        backgroundColor: '#F0F0F0',
        marginRight: 8,
        borderWidth: 1,
        borderColor: '#E0E0E0',
    },
    chipActive: {
        backgroundColor: '#FFF',
        borderColor: '#F26522', // Orange Border for active
    },
    chipText: {
        color: '#666',
        fontSize: 14,
    },
    chipTextActive: {
        color: '#F26522',
        fontWeight: 'bold',
    },
    listContent: {
        padding: 16,
    },
    card: {
        backgroundColor: '#FFFFFF',
        borderRadius: 12,
        padding: 16,
        marginBottom: 16,
        // Shadow/Elevation
        elevation: 3,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.1,
        shadowRadius: 4,
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
        color: '#004070', // Navy Blue
        marginBottom: 4,
    },
    student: {
        fontSize: 14,
        color: '#666666',
    },
    badge: {
        paddingHorizontal: 10,
        paddingVertical: 4,
        borderRadius: 12,
        alignSelf: 'flex-start',
    },
    badgeText: {
        fontSize: 12,
        fontWeight: 'bold',
    },
    actionContainer: {
        borderTopWidth: 1,
        borderTopColor: '#EEEEEE',
        paddingTop: 12,
    },
    button: {
        backgroundColor: '#FFFFFF',
        paddingVertical: 10,
        borderRadius: 8,
        alignItems: 'center',
        borderWidth: 1,
        borderColor: '#F26522', // FPT Orange Border
    },
    buttonText: {
        color: '#004070', // Navy Blue Text
        fontWeight: 'bold',
        fontSize: 14,
    },
    emptyText: {
        textAlign: 'center',
        marginTop: 20,
        color: '#888',
        fontStyle: 'italic',
    }
});
