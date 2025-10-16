import React, { useEffect, useState } from 'react';
import { View, ScrollView, ActivityIndicator, Pressable, Text } from 'react-native';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { ThemedView } from '@/components/themed-view';
import { ThemedText } from '@/components/themed-text';
import { Colors } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { fetchProductById, Product } from '@/services/productService';
import { ImageWithFallback } from '@/components/ImageWithFallback';

export default function ProductPage() {
  const { id } = useLocalSearchParams();
  const router = useRouter();
  const colorScheme = useColorScheme() ?? 'light';
  const theme = Colors[colorScheme];
  const [product, setProduct] = useState<Product | undefined>(undefined);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let mounted = true;
    const numericId = typeof id === 'string' ? parseInt(id, 10) : undefined;
    if (!numericId) {
      setLoading(false);
      return;
    }
    fetchProductById(numericId).then((p) => {
      if (mounted) {
        setProduct(p);
        setLoading(false);
      }
    });
    return () => { mounted = false };
  }, [id]);

  const handlePurchase = () => {
    // TODO: Implémenter la logique d'achat
    console.log('Achat du produit:', product?.id);
  };

  if (loading) {
    return (
      <ThemedView style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
        <ActivityIndicator size="large" color={theme.tint} />
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
        {/* Image principale */}
        <ImageWithFallback source={product.image} width={'100%'} height={240} borderRadius={12} />

        {/* Statut en ligne */}
        <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
          <View style={{ width: 10, height: 10, borderRadius: 10, backgroundColor: product.online ? '#34d399' : '#6b7280' }} />
          <Text style={{ color: product.online ? '#34d399' : '#9CA3AF', fontSize: 12 }}>
            {product.online ? 'En ligne' : 'Hors ligne'}
          </Text>
        </View>

        {/* Titre et fournisseur */}
        <View>
          <ThemedText type="title" style={{ marginBottom: 4 }}>{product.title}</ThemedText>
          <ThemedText style={{ color: '#a78bfa', fontSize: 14 }}>{`par ${product.provider}`}</ThemedText>
        </View>

        {/* Badges */}
        <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 8 }}>
          {product.badges.map((badge, idx) => (
            <View key={idx} style={{ backgroundColor: 'rgba(147,51,234,0.2)', paddingHorizontal: 12, paddingVertical: 6, borderRadius: 999 }}>
              <Text style={{ color: '#C4B5FD', fontSize: 12, fontWeight: '600' }}>{badge}</Text>
            </View>
          ))}
        </View>

        {/* Informations */}
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

        {/* Description */}
        <View>
          <ThemedText type="subtitle" style={{ marginBottom: 8 }}>Description</ThemedText>
          <ThemedText style={{ color: '#D1D5DB', lineHeight: 22 }}>{product.description}</ThemedText>
        </View>

        {/* Bouton d'achat */}
        <Pressable
          onPress={handlePurchase}
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

