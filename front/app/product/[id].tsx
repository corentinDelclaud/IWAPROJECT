// file: `front/app/product/[id].tsx`
import React, { useEffect, useState } from 'react';
import { View, ScrollView, ActivityIndicator, Pressable, Text, Dimensions, Alert } from 'react-native';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { ThemedView } from '@/components/themed-view';
import { ThemedText } from '@/components/themed-text';
import { Colors } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { fetchProductById, Product } from '@/services/productService';
import { createTransaction } from '@/services/transactionService';
import { ImageWithFallback } from '@/components/ImageWithFallback';
import { useAuth } from '@/context/AuthContext';

export default function ProductPage() {
    const params = useLocalSearchParams() as Record<string, any>;
    const rawId = params?.id ?? params?.productId ?? params?.pid;
    const router = useRouter();
    const colorScheme = useColorScheme() ?? 'light';
    const theme = Colors[colorScheme];
    const [product, setProduct] = useState<Product | null>(null);
    const [loading, setLoading] = useState(true);
    const [purchasing, setPurchasing] = useState(false);
    
    const { isAuthenticated, userInfo } = useAuth();

    const screenWidth = Dimensions.get('window').width - 32;

    useEffect(() => {
        let mounted = true;

        console.log('🔍 ProductPage - raw id received:', rawId, typeof rawId);

        const numericId = rawId == null ? Number.NaN : Number(rawId);
        console.log('🔍 ProductPage - numeric id:', numericId);

        if (Number.isNaN(numericId)) {
            console.log('❌ ID invalid or missing');
            setProduct(null);
            setLoading(false);
            return;
        }

        setLoading(true);
        fetchProductById(numericId)
            .then((result) => {
                console.log('✅ Result from fetch:', result);
                if (mounted) {
                    setProduct(result);
                    setLoading(false);
                }
            })
            .catch((err) => {
                console.error('❌ Fetch error:', err);
                if (mounted) {
                    setProduct(null);
                    setLoading(false);
                }
            });

        return () => { mounted = false; };
    }, [rawId]);

    const handlePurchase = async (directRequest: boolean) => {
        if (!product) return;

        if (!isAuthenticated) {
            Alert.alert(
                'Connexion requise',
                'Vous devez être connecté pour acheter un service.',
                [
                    { text: 'Annuler', style: 'cancel' },
                    { text: 'Se connecter', onPress: () => router.push('/login') },
                ]
            );
            return;
        }

        setPurchasing(true);
        try {
            console.log('🛒 Creating transaction for product:', product.id);
            const transaction = await createTransaction({
                serviceId: product.id,
                directRequest: directRequest,
            });
            
            console.log('✅ Transaction created:', transaction);
            
            Alert.alert(
                'Succès',
                directRequest 
                    ? 'Votre demande de réservation a été envoyée au vendeur.'
                    : 'La conversation a été créée. Vous pouvez discuter avec le vendeur.',
                [
                    { 
                        text: 'Voir la conversation', 
                        onPress: () => router.push(`/conversation/${transaction.id}` as any)
                    },
                ]
            );
        } catch (error: any) {
            console.error('❌ Purchase error:', error);
            
            let errorMessage = 'Une erreur est survenue lors de la création de la transaction.';
            if (error.message?.includes('Active transaction already exists')) {
                errorMessage = 'Vous avez déjà une transaction en cours pour ce service.';
            } else if (error.message?.includes('not available')) {
                errorMessage = 'Ce service n\'est plus disponible.';
            }
            
            Alert.alert('Erreur', errorMessage);
        } finally {
            setPurchasing(false);
        }
    };

    if (loading) {
        return (
            <ThemedView style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
                <ActivityIndicator size="large" color={theme.tint} />
                <ThemedText style={{ marginTop: 8 }}>Chargement...</ThemedText>
            </ThemedView>
        );
    }

    if (!product) {
        return (
            <ThemedView style={{ flex: 1, padding: 16 }}>
                <ThemedText type="title">Produit introuvable</ThemedText>
                <ThemedText>Le produit demandé n'existe pas.</ThemedText>
                <Pressable
                    onPress={() => router.back()}
                    style={{
                        marginTop: 16,
                        backgroundColor: theme.tint,
                        paddingVertical: 12,
                        paddingHorizontal: 24,
                        borderRadius: 10,
                        alignSelf: 'flex-start',
                    }}
                >
                    <Text style={{ color: 'white', fontWeight: '600' }}>Retour</Text>
                </Pressable>
            </ThemedView>
        );
    }

    return (
        <ThemedView style={{ flex: 1 }}>
            <ScrollView contentContainerStyle={{ padding: 16, gap: 16 }}>
                <ImageWithFallback source={product.image} width={screenWidth} height={240} borderRadius={12} />

                <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
                    <View style={{ width: 10, height: 10, borderRadius: 10, backgroundColor: product.online ? '#34d399' : '#6b7280' }} />
                    <Text style={{ color: product.online ? '#34d399' : '#9CA3AF', fontSize: 12 }}>
                        {product.online ? 'En ligne' : 'Hors ligne'}
                    </Text>
                </View>

                <View>
                    <ThemedText type="title" style={{ marginBottom: 4 }}>{product.title}</ThemedText>
                    <ThemedText style={{ color: '#a78bfa', fontSize: 14 }}>{`par ${product.provider}`}</ThemedText>
                </View>

                <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 8 }}>
                    {product.badges.map((badge, idx) => (
                        <View key={idx} style={{ backgroundColor: 'rgba(147,51,234,0.2)', paddingHorizontal: 12, paddingVertical: 6, borderRadius: 999 }}>
                            <Text style={{ color: '#C4B5FD', fontSize: 12, fontWeight: '600' }}>{badge}</Text>
                        </View>
                    ))}
                </View>

                <View style={{ backgroundColor: theme.slateCard, borderRadius: 12, padding: 16, borderWidth: 1, borderColor: theme.slateBorder, gap: 12 }}>
                    <View style={{ flexDirection: 'row', justifyContent: 'space-between' }}>
                        <View>
                            <Text style={{ color: '#9CA3AF', fontSize: 12 }}>Prix</Text>
                            <Text style={{ color: '#34d399', fontSize: 20, fontWeight: '700' }}>{product.price}</Text>
                        </View>
                        <View>
                            <Text style={{ color: '#9CA3AF', fontSize: 12 }}>Note</Text>
                            <Text style={{ color: '#FBBF24', fontSize: 16, fontWeight: '600' }}>⭐ {product.rating} ({product.reviews})</Text>
                        </View>
                    </View>

                    <View style={{ height: 1, backgroundColor: theme.slateBorder }} />

                    <View>
                        <Text style={{ color: '#9CA3AF', fontSize: 12 }}>Délai de livraison</Text>
                        <Text style={{ color: theme.text, fontSize: 14, fontWeight: '600', marginTop: 4 }}>{product.delivery}</Text>
                    </View>

                    <View>
                        <Text style={{ color: '#9CA3AF', fontSize: 12 }}>Catégorie</Text>
                        <Text style={{ color: theme.text, fontSize: 14, fontWeight: '600', marginTop: 4, textTransform: 'capitalize' }}>{product.category}</Text>
                    </View>
                </View>

                <View>
                    <ThemedText type="subtitle" style={{ marginBottom: 8 }}>Description</ThemedText>
                    <ThemedText style={{ color: '#D1D5DB', lineHeight: 22 }}>{product.description}</ThemedText>
                </View>

                <Pressable
                    onPress={() => handlePurchase(true)}
                    style={{
                        backgroundColor: '#6d28d9',
                        paddingVertical: 16,
                        paddingHorizontal: 24,
                        borderRadius: 12,
                        alignItems: 'center',
                        marginTop: 8,
                        marginBottom: 32,
                    }}
                >
                    <Text style={{ color: 'white', fontSize: 16, fontWeight: '700' }}>Acheter ce service</Text>
                </Pressable>
            </ScrollView>
        </ThemedView>
    );
}
