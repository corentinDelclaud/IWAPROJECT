import React, { useMemo, useState } from 'react';
import { Pressable, Text, View, ScrollView, Modal, TextInput } from 'react-native';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Colors } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { useTranslation } from 'react-i18next';

interface UserService {
    id: number;
    title: string;
    description: string;
    price: string;
    game: string;
    category: string;
    available: boolean;
    createdAt: string;
}

const GAMES = [
    { id: 'LEAGUE_OF_LEGENDS', name: 'League of Legends', icon: '⚔️' },
    { id: 'TEAMFIGHT_TACTICS', name: 'Teamfight Tactics', icon: '🛡️' },
    { id: 'ROCKET_LEAGUE', name: 'Rocket League', icon: '🚗' },
    { id: 'VALORANT', name: 'Valorant', icon: '🎯' },
    { id: 'OTHER', name: 'Autre', icon: '🔰' },
];

const CATEGORIES = [
    { id: 'BOOST', name: 'Boost' },
    { id: 'COACHING', name: 'Coaching' },
    { id: 'ACCOUNT_RESALING', name: 'Revente de compte' },
    { id: 'OTHER', name: 'Autre' },
];

export default function ServicesManagementScreen() {
    const colorScheme = useColorScheme() ?? 'light';
    const theme = Colors[colorScheme];
    const { t } = useTranslation('orders');
    const [activeTab, setActiveTab] = useState<'all' | 'available' | 'unavailable'>('all');
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [services, setServices] = useState<UserService[]>([
        { id: 1, title: 'Coaching Valorant', description: 'Session personnalisée 1h', price: '30€', game: 'VALORANT', category: 'COACHING', available: true, createdAt: '15/01/2025' },
        { id: 2, title: 'Boost LoL', description: 'Boost de rang rapide', price: '60€', game: 'LEAGUE_OF_LEGENDS', category: 'BOOST', available: true, createdAt: '14/01/2025' },
        { id: 3, title: 'Carry CS2', description: 'Premier Mode carry', price: '45€', game: 'OTHER', category: 'COACHING', available: false, createdAt: '13/01/2025' },
        { id: 4, title: 'Duo Queue Valorant', description: 'Partie en duo 2h', price: '25€', game: 'VALORANT', category: 'BOOST', available: true, createdAt: '12/01/2025' },
        { id: 5, title: 'Analyse VOD', description: 'Analyse de replay détaillée', price: '40€', game: 'VALORANT', category: 'COACHING', available: false, createdAt: '11/01/2025' },
    ]);

    const [newService, setNewService] = useState({
        title: '',
        description: '',
        price: '',
        game: 'VALORANT',
        category: 'COACHING',
    });

    const filteredServices = useMemo(() => {
        if (activeTab === 'all') return services;
        if (activeTab === 'available') return services.filter(s => s.available);
        return services.filter(s => !s.available);
    }, [activeTab, services]);

    const toggleAvailability = (id: number) => {
        setServices(prev => prev.map(s => s.id === id ? { ...s, available: !s.available } : s));
    };

    const deleteService = (id: number) => {
        setServices(prev => prev.filter(s => s.id !== id));
    };

    const createService = () => {
        if (!newService.title || !newService.price) {
            alert('Veuillez remplir au moins le titre et le prix');
            return;
        }

        const service: UserService = {
            id: Math.max(...services.map(s => s.id), 0) + 1,
            title: newService.title,
            description: newService.description,
            price: newService.price.includes('€') ? newService.price : `${newService.price}€`,
            game: newService.game,
            category: newService.category,
            available: true,
            createdAt: new Date().toLocaleDateString('fr-FR'),
        };

        setServices(prev => [service, ...prev]);
        setShowCreateModal(false);
        setNewService({ title: '', description: '', price: '', game: 'VALORANT', category: 'COACHING' });
    };

    const getAvailabilityChip = (available: boolean) => {
        const config = available
            ? { text: 'Disponible', color: 'rgba(34,197,94,0.15)', fg: '#22c55e' }
            : { text: 'Indisponible', color: 'rgba(239,68,68,0.15)', fg: '#ef4444' };

        return (
            <View style={{ flexDirection: 'row', alignItems: 'center', paddingHorizontal: 10, paddingVertical: 4, borderRadius: 999, backgroundColor: config.color }}>
                <Text style={{ color: config.fg, fontSize: 12, fontWeight: '600' }}>{config.text}</Text>
            </View>
        );
    };

    const getGameName = (gameId: string) => GAMES.find(g => g.id === gameId)?.name || gameId;
    const getCategoryName = (categoryId: string) => CATEGORIES.find(c => c.id === categoryId)?.name || categoryId;

    return (
        <ThemedView style={{ flex: 1 }}>
            <ScrollView style={{ flex: 1 }} contentContainerStyle={{ padding: 16 }}>
                <View style={{ backgroundColor: theme.slateCard, borderRadius: 14, padding: 16, borderWidth: 1, borderColor: theme.slateBorder, marginBottom: 16 }}>
                    <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
                        <View>
                            <ThemedText type="title">Mes Services</ThemedText>
                            <ThemedText style={{ color: '#9CA3AF', marginTop: 4 }}>Gérez vos services proposés</ThemedText>
                        </View>
                        <Pressable
                            onPress={() => setShowCreateModal(true)}
                            style={{ paddingVertical: 10, paddingHorizontal: 16, backgroundColor: '#8b5cf6', borderRadius: 10 }}
                        >
                            <Text style={{ color: 'white', fontWeight: '600' }}>Nouveau service</Text>
                        </Pressable>
                    </View>
                </View>

                <View style={{ backgroundColor: theme.slateCard, borderRadius: 12, borderWidth: 1, borderColor: theme.slateBorder, padding: 8, marginBottom: 16 }}>
                    <View style={{ flexDirection: 'row', gap: 8 }}>
                        {[
                            { id: 'all', label: 'Tous', count: services.length },
                            { id: 'available', label: 'Disponibles', count: services.filter(s => s.available).length },
                            { id: 'unavailable', label: 'Indisponibles', count: services.filter(s => !s.available).length },
                        ].map((tab) => (
                            <Pressable
                                key={tab.id}
                                onPress={() => setActiveTab(tab.id as any)}
                                style={{
                                    paddingVertical: 10,
                                    paddingHorizontal: 14,
                                    borderRadius: 10,
                                    borderWidth: 1,
                                    borderColor: activeTab === tab.id ? 'rgba(168,85,247,0.5)' : theme.slateBorder,
                                    backgroundColor: activeTab === tab.id ? 'rgba(147,51,234,0.15)' : 'transparent',
                                    flexDirection: 'row',
                                    alignItems: 'center',
                                    gap: 8,
                                }}
                            >
                                <Text style={{ color: theme.text }}>{tab.label}</Text>
                                <View style={{ backgroundColor: 'rgba(71,85,105,0.5)', paddingHorizontal: 8, paddingVertical: 2, borderRadius: 999 }}>
                                    <Text style={{ color: theme.text, fontSize: 12 }}>{tab.count}</Text>
                                </View>
                            </Pressable>
                        ))}
                    </View>
                </View>

                {filteredServices.map((item) => (
                    <View key={item.id} style={{ backgroundColor: theme.slateCard, borderRadius: 12, borderWidth: 1, borderColor: theme.slateBorder, padding: 16, marginBottom: 12 }}>
                        <View style={{ flexDirection: 'row', justifyContent: 'space-between', marginBottom: 8 }}>
                            <View style={{ flex: 1 }}>
                                <Text style={{ color: theme.text, fontWeight: '600', fontSize: 16 }}>{item.title}</Text>
                                <Text style={{ color: '#9CA3AF', marginTop: 2, fontSize: 12 }}>
                                    {getGameName(item.game)} • {getCategoryName(item.category)}
                                </Text>
                            </View>
                            <View style={{ alignItems: 'flex-end' }}>
                                <Text style={{ color: '#34d399', fontWeight: '700', fontSize: 16 }}>{item.price}</Text>
                                <Text style={{ color: '#9CA3AF', fontSize: 12 }}>{item.createdAt}</Text>
                            </View>
                        </View>

                        <Text style={{ color: '#D1D5DB', fontSize: 12, marginBottom: 10 }}>{item.description}</Text>

                        <View style={{ flexDirection: 'row', alignItems: 'center', gap: 12, marginBottom: 12 }}>
                            {getAvailabilityChip(item.available)}
                        </View>

                        <View style={{ flexDirection: 'row', gap: 8 }}>
                            <Pressable
                                onPress={() => toggleAvailability(item.id)}
                                style={{ flex: 1, paddingVertical: 10, backgroundColor: item.available ? 'rgba(239,68,68,0.2)' : 'rgba(34,197,94,0.2)', borderRadius: 10, borderWidth: 1, borderColor: item.available ? 'rgba(239,68,68,0.3)' : 'rgba(34,197,94,0.3)' }}
                            >
                                <Text style={{ color: item.available ? '#ef4444' : '#22c55e', textAlign: 'center', fontWeight: '600' }}>
                                    {item.available ? 'Désactiver' : 'Activer'}
                                </Text>
                            </Pressable>
                            <Pressable
                                style={{ paddingVertical: 10, paddingHorizontal: 16, backgroundColor: 'rgba(71,85,105,0.5)', borderRadius: 10, borderWidth: 1, borderColor: theme.slateBorder }}
                            >
                                <Text style={{ color: theme.text }}>Modifier</Text>
                            </Pressable>
                            <Pressable
                                onPress={() => deleteService(item.id)}
                                style={{ paddingVertical: 10, paddingHorizontal: 16, backgroundColor: 'rgba(239,68,68,0.2)', borderRadius: 10, borderWidth: 1, borderColor: 'rgba(239,68,68,0.3)' }}
                            >
                                <Text style={{ color: '#ef4444' }}>Supprimer</Text>
                            </Pressable>
                        </View>
                    </View>
                ))}
            </ScrollView>

            {/* Modal de création */}
            <Modal
                visible={showCreateModal}
                transparent
                animationType="slide"
                onRequestClose={() => setShowCreateModal(false)}
            >
                <View style={{ flex: 1, backgroundColor: 'rgba(0,0,0,1)', justifyContent: 'center', padding: 20 }}>
                    <View style={{ backgroundColor: theme.slateCard, borderRadius: 16, padding: 20, maxHeight: '80%' }}>
                        <Text style={{ color: theme.text, fontSize: 20, fontWeight: '700', marginBottom: 20 }}>Créer un nouveau service</Text>

                        <ScrollView>
                            <Text style={{ color: theme.text, marginBottom: 8 }}>Titre *</Text>
                            <TextInput
                                value={newService.title}
                                onChangeText={(text) => setNewService(prev => ({ ...prev, title: text }))}
                                placeholder="Ex: Coaching Valorant"
                                placeholderTextColor="#666"
                                style={{ backgroundColor: theme.background, color: theme.text, padding: 12, borderRadius: 8, marginBottom: 16, borderWidth: 1, borderColor: theme.slateBorder }}
                            />

                            <Text style={{ color: theme.text, marginBottom: 8 }}>Description</Text>
                            <TextInput
                                value={newService.description}
                                onChangeText={(text) => setNewService(prev => ({ ...prev, description: text }))}
                                placeholder="Décrivez votre service"
                                placeholderTextColor="#666"
                                multiline
                                numberOfLines={3}
                                style={{ backgroundColor: theme.background, color: theme.text, padding: 12, borderRadius: 8, marginBottom: 16, borderWidth: 1, borderColor: theme.slateBorder, textAlignVertical: 'top' }}
                            />

                            <Text style={{ color: theme.text, marginBottom: 8 }}>Prix *</Text>
                            <TextInput
                                value={newService.price}
                                onChangeText={(text) => setNewService(prev => ({ ...prev, price: text }))}
                                placeholder="Ex: 30"
                                placeholderTextColor="#666"
                                keyboardType="numeric"
                                style={{ backgroundColor: theme.background, color: theme.text, padding: 12, borderRadius: 8, marginBottom: 16, borderWidth: 1, borderColor: theme.slateBorder }}
                            />

                            <Text style={{ color: theme.text, marginBottom: 8 }}>Jeu *</Text>
                            <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 8, marginBottom: 16 }}>
                                {GAMES.map((game) => (
                                    <Pressable
                                        key={game.id}
                                        onPress={() => setNewService(prev => ({ ...prev, game: game.id }))}
                                        style={{
                                            paddingVertical: 8,
                                            paddingHorizontal: 12,
                                            borderRadius: 8,
                                            borderWidth: 1,
                                            borderColor: newService.game === game.id ? '#8b5cf6' : theme.slateBorder,
                                            backgroundColor: newService.game === game.id ? 'rgba(139,92,246,0.2)' : 'transparent',
                                        }}
                                    >
                                        <Text style={{ color: theme.text }}>
                                            {game.icon} {game.name}
                                        </Text>
                                    </Pressable>
                                ))}
                            </View>

                            <Text style={{ color: theme.text, marginBottom: 8 }}>Catégorie *</Text>
                            <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 8, marginBottom: 20 }}>
                                {CATEGORIES.map((category) => (
                                    <Pressable
                                        key={category.id}
                                        onPress={() => setNewService(prev => ({ ...prev, category: category.id }))}
                                        style={{
                                            paddingVertical: 8,
                                            paddingHorizontal: 12,
                                            borderRadius: 8,
                                            borderWidth: 1,
                                            borderColor: newService.category === category.id ? '#8b5cf6' : theme.slateBorder,
                                            backgroundColor: newService.category === category.id ? 'rgba(139,92,246,0.2)' : 'transparent',
                                        }}
                                    >
                                        <Text style={{ color: theme.text }}>{category.name}</Text>
                                    </Pressable>
                                ))}
                            </View>
                        </ScrollView>

                        <View style={{ flexDirection: 'row', gap: 12 }}>
                            <Pressable
                                onPress={() => setShowCreateModal(false)}
                                style={{ flex: 1, paddingVertical: 12, backgroundColor: 'rgba(239,68,68,0.2)', borderRadius: 10, borderWidth: 1, borderColor: 'rgba(239,68,68,0.3)' }}
                            >
                                <Text style={{ color: '#ef4444', textAlign: 'center', fontWeight: '600' }}>Annuler</Text>
                            </Pressable>
                            <Pressable
                                onPress={createService}
                                style={{ flex: 1, paddingVertical: 12, backgroundColor: '#8b5cf6', borderRadius: 10 }}
                            >
                                <Text style={{ color: 'white', textAlign: 'center', fontWeight: '600' }}>Créer</Text>
                            </Pressable>
                        </View>
                    </View>
                </View>
            </Modal>
        </ThemedView>
    );
}