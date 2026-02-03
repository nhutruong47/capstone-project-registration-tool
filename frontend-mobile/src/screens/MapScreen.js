import React from 'react';
import { View, StyleSheet, Dimensions, Alert } from 'react-native';
import MapView, { Marker } from 'react-native-maps';

const MapScreen = () => {
    // Mock data for markers
    const markers = [
        {
            id: 1,
            title: 'Lab AI & IoT',
            description: 'Nghiên cứu ứng dụng AI trong Y tế',
            coordinate: { latitude: 10.762622, longitude: 106.660172 }, // Example: HCMC University area
        },
        {
            id: 2,
            title: 'Lab Bảo mật',
            description: 'Đề tài Blockchain Voting',
            coordinate: { latitude: 10.772622, longitude: 106.670172 },
        },
        {
            id: 3,
            title: 'Khu thực nghiệm',
            description: 'Triển khai Smart Home',
            coordinate: { latitude: 10.752622, longitude: 106.650172 },
        },
    ];

    const handleMarkerPress = (marker) => {
        // You can also use the default Callout, but here's an alert as per requirement "Quick Info"
        // Using default title/description props on Marker works for basic info too.
    };

    return (
        <View style={styles.container}>
            <MapView
                style={styles.map}
                initialRegion={{
                    latitude: 10.762622,
                    longitude: 106.660172,
                    latitudeDelta: 0.05,
                    longitudeDelta: 0.05,
                }}
            >
                {markers.map((marker) => (
                    <Marker
                        key={marker.id}
                        coordinate={marker.coordinate}
                        title={marker.title}
                        description={marker.description}
                        onPress={() => handleMarkerPress(marker)}
                    />
                ))}
            </MapView>
        </View>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#fff',
        alignItems: 'center',
        justifyContent: 'center',
    },
    map: {
        width: Dimensions.get('window').width,
        height: Dimensions.get('window').height,
    },
});

export default MapScreen;
