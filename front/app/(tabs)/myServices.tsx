import React, { useMemo, useState, useEffect } from 'react';
import { Pressable, Text, View, ScrollView, Alert } from 'react-native';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Colors } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { fetchProductsByProvider, deleteProduct } from '@/services/productService';
import { apiService } from '@/services/api';
import AddProductModal from '@/components/AddProductModal';

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
    const textSecondary = colorScheme === 'dark' ? (theme as typeof Colors.dark).textSecondary : theme.icon;
    const [activeTab, setActiveTab] = useState<'all' | 'available' | 'unavailable'>('all');
    const [services, setServices] = useState<UserService[]>([]);
    const [loading, setLoading] = useState(true);
    const [userId, setUserId] = useState<string | null>(null);
    const [showAddModal, setShowAddModal] = useState(false);

    useEffect(() => {
        initializeAndLoadServices();
    }, []);

    useEffect(() => {
        console.log('🔄 showAddModal changé:', showAddModal);
    }, [showAddModal]);

    useEffect(() => {
        console.log('👤 userId changé:', userId);
    }, [userId]);

    const initializeAndLoadServices = async () => {
        try {
            // Récupérer l'ID utilisateur depuis le profil
            const profile = await apiService.getUserProfile();
            setUserId(profile.id);

            await loadUserProducts(profile.id);
        } catch (error) {
            console.error('Error initializing:', error);
            Alert.alert('Erreur', 'Impossible de charger vos informations');
            setLoading(false);
        }
    };

    const loadUserProducts = async (providerId: string) => {
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

    return (
        <ThemedView style={{ flex: 1 }}>
            {/* En-tête fixe avec bouton d'ajout et tabs */}
            <View style={{ padding: 16, paddingBottom: 8 }}>
                {/* Bouton Ajouter un produit */}
                <Pressable
                    onPress={() => {
                        console.log('📝 Bouton Ajouter un produit cliqué');
                        console.log('📝 userId:', userId);
                        console.log('📝 showAddModal avant:', showAddModal);
                        setShowAddModal(true);
                        console.log('📝 setShowAddModal(true) appelé');
                    }}
                    style={{
                        backgroundColor: '#9333EA', // Violet vif pour meilleure visibilité
                        padding: 16,
                        borderRadius: 12,
                        marginBottom: 12,
                        flexDirection: 'row',
                        alignItems: 'center',
                        justifyContent: 'center',
                        gap: 8,
                        shadowColor: '#9333EA',
                        shadowOffset: { width: 0, height: 4 },
                        shadowOpacity: 0.3,
                        shadowRadius: 8,
                        elevation: 4,
                    }}
                >
                    <Text style={{ color: '#fff', fontSize: 18, fontWeight: '700' }}>+ Ajouter un produit</Text>
                </Pressable>

                {/* Tabs */}
                <View style={{ flexDirection: 'row', gap: 8 }}>
                    {(['all', 'available', 'unavailable'] as const).map(tab => (
                        <Pressable
                            key={tab}
                            onPress={() => setActiveTab(tab)}
                            style={{
                                flex: 1,
                                paddingVertical: 10,
                                paddingHorizontal: 16,
                                borderRadius: 10,
                                borderWidth: 1,
                                borderColor: activeTab === tab ? 'rgba(168,85,247,0.5)' : theme.slateBorder,
                                backgroundColor: activeTab === tab ? 'rgba(147,51,234,0.15)' : 'rgba(51,65,85,0.5)',
                            }}
                        >
                            <Text style={{
                                color: activeTab === tab ? theme.tint : theme.text,
                                fontWeight: '600',
                                textAlign: 'center'
                            }}>
                                {tab === 'all' ? 'Tous' : tab === 'available' ? 'Disponibles' : 'Indisponibles'}
                            </Text>
                        </Pressable>
                    ))}
                </View>
            </View>

            {/* Liste des services */}
            {loading ? (
                <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
                    <Text style={{ color: theme.text }}>Chargement de vos services...</Text>
                </View>
            ) : (
                <ScrollView style={{ flex: 1 }} contentContainerStyle={{ padding: 16, paddingTop: 8 }}>

                {/* Services List */}
                {filteredServices.map(service => (
                    <View key={service.id} style={{ backgroundColor: theme.slateCard, borderRadius: 12, padding: 16, marginBottom: 12 }}>
                        <View style={{ flexDirection: 'row', justifyContent: 'space-between', marginBottom: 8 }}>
                            <ThemedText style={{ fontSize: 18, fontWeight: '600' }}>{service.title}</ThemedText>
                            <View style={{ flexDirection: 'row', alignItems: 'center', paddingHorizontal: 10, paddingVertical: 4, borderRadius: 999, backgroundColor: service.online ? 'rgba(34,197,94,0.15)' : 'rgba(239,68,68,0.15)' }}>
                                <Text style={{ color: service.online ? '#22c55e' : '#ef4444', fontSize: 12, fontWeight: '600' }}>
                                    {service.online ? 'Disponible' : 'Indisponible'}
                                </Text>
                            </View>
                        </View>

                        <Text style={{ color: textSecondary, marginBottom: 12 }}>{service.description}</Text>

                        <View style={{ flexDirection: 'row', gap: 8, marginBottom: 12 }}>
                            <View style={{ paddingHorizontal: 8, paddingVertical: 4, borderRadius: 6, backgroundColor: 'rgba(99,102,241,0.1)' }}>
                                <Text style={{ color: '#6366f1', fontSize: 12 }}>{service.game}</Text>
                            </View>
                            <View style={{ paddingHorizontal: 8, paddingVertical: 4, borderRadius: 6, backgroundColor: 'rgba(236,72,153,0.1)' }}>
                                <Text style={{ color: '#ec4899', fontSize: 12 }}>{service.category}</Text>
                            </View>
                        </View>

                        <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
                            <ThemedText style={{ fontSize: 20, fontWeight: '700', color: theme.tint }}>{service.price}</ThemedText>

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
                            <Text style={{ color: textSecondary }}>Aucun service trouvé</Text>
                        </View>
                    )}
                </ScrollView>
            )}

            {/* Modal pour ajouter un produit - Toujours disponible */}
            <AddProductModal
                visible={showAddModal}
                onClose={() => {
                    console.log('📝 Fermeture du modal');
                    setShowAddModal(false);
                }}
                onProductAdded={() => {
                    console.log('📝 Produit ajouté, rechargement de la liste');
                    if (userId) {
                        loadUserProducts(userId);
                    }
                }}
                userId={userId || ''}
            />
        </ThemedView>
    );
}