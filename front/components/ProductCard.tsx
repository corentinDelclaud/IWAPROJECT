import React from "react";
import { View, Text, Pressable } from "react-native";
import { ThemedView } from "@/components/themed-view";
import { Colors } from "@/constants/theme";
import { useColorScheme } from "@/hooks/use-color-scheme";
import { ImageWithFallback } from "@/components/ImageWithFallback";

export type Product = {
  id: number;
  title: string;
  provider: string;
  game: string;
  category: string;
  price: string;
  rating: number;
  reviews: number;
  description: string;
  image: any;  // Peut être une string (URI) ou un require() local
  badges: string[];
  delivery: string;
  online: boolean;
};

export default function ProductCard({
  product,
  onPress,
}: {
  product: Product;
  onPress?: () => void;
}) {
  const colorScheme = useColorScheme() ?? "light";
  const theme = Colors[colorScheme];

  return (
    <ThemedView style={{ backgroundColor: theme.slateCard, borderRadius: 12, borderWidth: 1, borderColor: theme.slateBorder, overflow: 'hidden' }}>
      <Pressable onPress={onPress} style={{ flexDirection: 'row', padding: 12 }}>
        <View style={{ position: 'relative' }}>
          <ImageWithFallback source={product.image} width={80} height={80} borderRadius={10} />
          <View style={{ position: 'absolute', top: 4, right: 4, width: 8, height: 8, borderRadius: 8, backgroundColor: product.online ? theme.green : '#6b7280' }} />
        </View>

        <View style={{ flex: 1, marginLeft: 12 }}>
          <View style={{ flexDirection: 'row', justifyContent: 'space-between', marginBottom: 6 }}>
            <View style={{ flex: 1, paddingRight: 8 }}>
              <Text style={{ color: theme.text, fontWeight: '600', fontSize: 14 }} numberOfLines={2}>{product.title}</Text>
              <Text style={{ color: '#a78bfa', fontSize: 12 }}>{`par ${product.provider}`}</Text>
            </View>
            <View style={{ alignItems: 'flex-end' }}>
              <Text style={{ color: '#34d399', fontWeight: '700', fontSize: 14 }}>{product.price}</Text>
            </View>
          </View>

          <Text style={{ color: '#D1D5DB', fontSize: 12, marginBottom: 8 }} numberOfLines={2}>{product.description}</Text>

          <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
            <View style={{ flexDirection: 'row', gap: 6 }}>
              {product.badges.slice(0, 2).map((badge, idx) => (
                <View key={idx} style={{ backgroundColor: 'rgba(147,51,234,0.2)', paddingHorizontal: 8, paddingVertical: 4, borderRadius: 999 }}>
                  <Text style={{ color: '#C4B5FD', fontSize: 10 }}>{badge}</Text>
                </View>
              ))}
            </View>
          </View>
        </View>
      </Pressable>
    </ThemedView>
  );
}

