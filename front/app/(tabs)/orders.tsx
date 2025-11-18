import React, { useMemo, useState, useEffect } from 'react';
import { Pressable, Text, View, ScrollView, Modal, TextInput, Alert } from 'react-native';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Colors } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { useTranslation } from 'react-i18next';
import { fetchProductsByProvider, deleteProduct } from '@/services/productService';
import { apiService } from '@/services/api';
import AsyncStorage from '@react-native-async-storage/async-storage';

interface UserService {
    id: number;
    title: string;
    description: string;
    price: string;
    game: string;
    category: string;
    provider: string;
    rating: number;
    online: boolean;
}

export default function ServicesManagementScreen() {
    const colorScheme = useColorScheme() ?? 'light';
    const theme = Colors[colorScheme];
    const { t } = useTranslation('orders');
    const [activeTab, setActiveTab] = useState<'all' | 'available' | 'unavailable'>('all');
    const [services, setServices] = useState<UserService[]>([]);
    const [loading, setLoading] = useState(true);
    const [userId, setUserId] = useState<number | null>(null);

    useEffect(() => {
        initializeAndLoadServices();
    }, []);

    const initializeAndLoadServices = async () => {
        try {
            // Récupérer l'ID utilisateur depuis le profil
            const profile = await apiService.getUserProfile();
            const userIdNum = parseInt(profile.id);
            setUserId(userIdNum);

            await loadUserProducts(userIdNum);
        } catch (error) {
            console.error('Error initializing:', error);
            Alert.alert('Erreur', 'Impossible de charger vos informations');
            setLoading(false);
        }
    };

    const loadUserProducts = async (providerId: number) => {
        setLoading(true);
        try {
            const products = await fetchProductsByProvider(providerId);
            const mapped = products.map(p => ({
                id: p.id,
                title: p.title,
                description: p.description,
                price: p.price,
                game: p.game,
                category: p.category,
                provider: p.provider,
                rating: p.rating,
                online: p.online,
            }));
            setServices(mapped);
        } catch (error) {
            console.error('Error loading products:', error);
            Alert.alert('Erreur', 'Impossible de charger vos services');
        } finally {
            setLoading(false);
        }
    };

    const filteredServices = useMemo(() => {
        if (activeTab === 'all') return services;
        if (activeTab === 'available') return services.filter(s => s.online);
        return services.filter(s => !s.online);
    }, [activeTab, services]);

    const handleDeleteService = async (id: number) => {
        Alert.alert(
            'Confirmation',
            'Voulez-vous vraiment supprimer ce service ?',
            [
                { text: 'Annuler', style: 'cancel' },
                {
                    text: 'Supprimer',
                    style: 'destructive',
                    onPress: async () => {
                        try {
                            const success = await deleteProduct(id);
                            if (success) {
                                setServices(prev => prev.filter(s => s.id !== id));
                                Alert.alert('Succès', 'Service supprimé');
                            } else {
                                Alert.alert('Erreur', 'Impossible de supprimer le service');
                            }
                        } catch (error) {
                            Alert.alert('Erreur', 'Une erreur est survenue');
                        }
                    },
                },
            ]
        );
    };

    if (loading) {
        return (
            <ThemedView style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
                <Text style={{ color: theme.text }}>Chargement...</Text>
            </ThemedView>
        );
    }

    return (
        <ThemedView style={{ flex: 1 }}>
            <ScrollView style={{ flex: 1 }} contentContainerStyle={{ padding: 16 }}>
                {/* Tabs */}
                <View style={{ flexDirection: 'row', gap: 8, marginBottom: 16 }}>
                    {(['all', 'available', 'unavailable'] as const).map(tab => (
                        <Pressable
                            key={tab}
                            onPress={() => setActiveTab(tab)}
                            style={{
                                flex: 1,
                                paddingVertical: 10,
                                paddingHorizontal: 16,
                                borderRadius: 8,
                                backgroundColor: activeTab === tab ? theme.primary : theme.card,
                            }}
                        >
                            <Text style={{ color: activeTab === tab ? '#fff' : theme.text, fontWeight: '600', textAlign: 'center' }}>
                                {tab === 'all' ? 'Tous' : tab === 'available' ? 'Disponibles' : 'Indisponibles'}
                            </Text>
                        </Pressable>
                    ))}
                </View>

                {/* Services List */}
                {filteredServices.map(service => (
                    <View key={service.id} style={{ backgroundColor: theme.card, borderRadius: 12, padding: 16, marginBottom: 12 }}>
                        <View style={{ flexDirection: 'row', justifyContent: 'space-between', marginBottom: 8 }}>
                            <ThemedText style={{ fontSize: 18, fontWeight: '600' }}>{service.title}</ThemedText>
                            <View style={{ flexDirection: 'row', alignItems: 'center', paddingHorizontal: 10, paddingVertical: 4, borderRadius: 999, backgroundColor: service.online ? 'rgba(34,197,94,0.15)' : 'rgba(239,68,68,0.15)' }}>
                                <Text style={{ color: service.online ? '#22c55e' : '#ef4444', fontSize: 12, fontWeight: '600' }}>
                                    {service.online ? 'Disponible' : 'Indisponible'}
                                </Text>
                            </View>
                        </View>

                        <Text style={{ color: theme.textSecondary, marginBottom: 12 }}>{service.description}</Text>

                        <View style={{ flexDirection: 'row', gap: 8, marginBottom: 12 }}>
                            <View style={{ paddingHorizontal: 8, paddingVertical: 4, borderRadius: 6, backgroundColor: 'rgba(99,102,241,0.1)' }}>
                                <Text style={{ color: '#6366f1', fontSize: 12 }}>{service.game}</Text>
                            </View>
                            <View style={{ paddingHorizontal: 8, paddingVertical: 4, borderRadius: 6, backgroundColor: 'rgba(236,72,153,0.1)' }}>
                                <Text style={{ color: '#ec4899', fontSize: 12 }}>{service.category}</Text>
                            </View>
                        </View>

                        <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
                            <ThemedText style={{ fontSize: 20, fontWeight: '700', color: theme.primary }}>{service.price}</ThemedText>

                            <Pressable
                                onPress={() => handleDeleteService(service.id)}
                                style={{ paddingVertical: 8, paddingHorizontal: 16, borderRadius: 8, backgroundColor: 'rgba(239,68,68,0.1)' }}
                            >
                                <Text style={{ color: '#ef4444', fontWeight: '600' }}>Supprimer</Text>
                            </Pressable>
                        </View>
                    </View>
                ))}

                {filteredServices.length === 0 && (
                    <View style={{ alignItems: 'center', paddingVertical: 32 }}>
                        <Text style={{ color: theme.textSecondary }}>Aucun service trouvé</Text>
                    </View>
                )}
            </ScrollView>
        </ThemedView>
    );
}