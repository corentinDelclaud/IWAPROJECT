import React from "react";
import { ScrollView, View, Text, Pressable } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useLocalSearchParams, useRouter } from "expo-router";
import { ThemedText } from "@/components/themed-text";
import { ThemedView } from "@/components/themed-view";
import { Colors } from "@/constants/theme";
import { useColorScheme } from "@/hooks/use-color-scheme";
import { ImageWithFallback } from "@/components/ImageWithFallback";
import { useTranslation } from "react-i18next";
import { Ionicons } from "@expo/vector-icons";
import { useService } from "@/hooks/useProducts";

export default function ProductDetailScreen() {
  const { id } = useLocalSearchParams();
  const router = useRouter();
  const colorScheme = useColorScheme() ?? "light";
  const theme = Colors[colorScheme];
  const { t } = useTranslation('marketplace');

  // Utilisation du hook personnalisé pour récupérer le service
  const { service, loading, error } = useService(parseInt(id as string));

  if (loading) {
    return (
      <ThemedView style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
        <ThemedText>Chargement du service...</ThemedText>
      </ThemedView>
    );
  }

  if (error || !service) {
    return (
      <ThemedView style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
        <ThemedText style={{ color: 'red' }}>{error || 'Service introuvable'}</ThemedText>
        <Pressable
          onPress={() => router.back()}
          style={{ marginTop: 16, padding: 12, backgroundColor: theme.slateCard, borderRadius: 8 }}
        >
          <ThemedText>Retour</ThemedText>
        </Pressable>
      </ThemedView>
    );
  }

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: theme.background }}>
      <ScrollView style={{ flex: 1 }} showsVerticalScrollIndicator={false}>
        {/* Header avec bouton retour */}
        <View style={{
          flexDirection: 'row',
          alignItems: 'center',
          padding: 16,
          backgroundColor: theme.slateCard,
          borderBottomWidth: 1,
          borderBottomColor: theme.slateBorder
        }}>
          <Pressable onPress={() => router.back()} style={{ marginRight: 12 }}>
            <Ionicons name="arrow-back" size={24} color={theme.text} />
          </Pressable>
          <ThemedText type="subtitle" style={{ flex: 1 }}>Détails du service</ThemedText>
        </View>

        {/* Image principale */}
        <View style={{ padding: 16 }}>
          <View style={{ position: 'relative', borderRadius: 12, overflow: 'hidden' }}>
            <ImageWithFallback source={service.image} width="100%" height={200} borderRadius={12} />
            <View style={{
              position: 'absolute',
              top: 12,
              right: 12,
              width: 12,
              height: 12,
              borderRadius: 12,
              backgroundColor: service.online ? theme.green : '#6b7280',
              borderWidth: 2,
              borderColor: 'white'
            }} />
          </View>
        </View>

        {/* Informations principales */}
        <View style={{ paddingHorizontal: 16, marginBottom: 16 }}>
          <View style={{ backgroundColor: theme.slateCard, borderRadius: 12, padding: 16, borderWidth: 1, borderColor: theme.slateBorder }}>
            <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 }}>
              <View style={{ flex: 1, paddingRight: 12 }}>
                <ThemedText type="subtitle" style={{ marginBottom: 4 }}>{service.title}</ThemedText>
                <Text style={{ color: '#a78bfa', fontSize: 14 }}>par {service.provider}</Text>
              </View>
              <Text style={{ color: '#34d399', fontWeight: '700', fontSize: 18 }}>{service.price}</Text>
            </View>

            <View style={{ flexDirection: 'row', alignItems: 'center', marginBottom: 12 }}>
              <Text style={{ color: '#FBBF24', fontSize: 14, fontWeight: '600', marginRight: 16 }}>
                ⭐ {service.rating} ({service.reviews} avis)
              </Text>
              <Text style={{ color: '#9CA3AF', fontSize: 14 }}>
                🚚 {service.delivery}
              </Text>
            </View>

            <View style={{ flexDirection: 'row', gap: 8, flexWrap: 'wrap' }}>
              {service.badges.map((badge, idx) => (
                <View key={idx} style={{ backgroundColor: 'rgba(147,51,234,0.2)', paddingHorizontal: 12, paddingVertical: 6, borderRadius: 999 }}>
                  <Text style={{ color: '#C4B5FD', fontSize: 12, fontWeight: '500' }}>{badge}</Text>
                </View>
              ))}
            </View>
          </View>
        </View>

        {/* Description détaillée */}
        <View style={{ paddingHorizontal: 16, marginBottom: 16 }}>
          <View style={{ backgroundColor: theme.slateCard, borderRadius: 12, padding: 16, borderWidth: 1, borderColor: theme.slateBorder }}>
            <ThemedText type="defaultSemiBold" style={{ marginBottom: 8 }}>Description</ThemedText>
            <Text style={{ color: '#D1D5DB', lineHeight: 20 }}>{service.longDescription}</Text>
          </View>
        </View>

        {/* Fonctionnalités incluses */}
        <View style={{ paddingHorizontal: 16, marginBottom: 16 }}>
          <View style={{ backgroundColor: theme.slateCard, borderRadius: 12, padding: 16, borderWidth: 1, borderColor: theme.slateBorder }}>
            <ThemedText type="defaultSemiBold" style={{ marginBottom: 12 }}>Ce qui est inclus</ThemedText>
            {service.features.map((feature, idx) => (
              <View key={idx} style={{ flexDirection: 'row', alignItems: 'center', marginBottom: 8 }}>
                <Text style={{ color: '#34d399', marginRight: 8, fontSize: 16 }}>✓</Text>
                <Text style={{ color: '#D1D5DB', flex: 1 }}>{feature}</Text>
              </View>
            ))}
          </View>
        </View>

        {/* Statistiques du prestataire */}
        <View style={{ paddingHorizontal: 16, marginBottom: 16 }}>
          <View style={{ backgroundColor: theme.slateCard, borderRadius: 12, padding: 16, borderWidth: 1, borderColor: theme.slateBorder }}>
            <ThemedText type="defaultSemiBold" style={{ marginBottom: 12 }}>À propos du prestataire</ThemedText>
            <View style={{ flexDirection: 'row', justifyContent: 'space-between', marginBottom: 8 }}>
              <Text style={{ color: '#9CA3AF' }}>Commandes réalisées</Text>
              <Text style={{ color: theme.text, fontWeight: '600' }}>{service.providerStats.totalOrders}</Text>
            </View>
            <View style={{ flexDirection: 'row', justifyContent: 'space-between', marginBottom: 8 }}>
              <Text style={{ color: '#9CA3AF' }}>Taux de réussite</Text>
              <Text style={{ color: '#34d399', fontWeight: '600' }}>{service.providerStats.completionRate}%</Text>
            </View>
            <View style={{ flexDirection: 'row', justifyContent: 'space-between', marginBottom: 8 }}>
              <Text style={{ color: '#9CA3AF' }}>Temps de réponse</Text>
              <Text style={{ color: theme.text, fontWeight: '600' }}>{service.providerStats.responseTime}</Text>
            </View>
            <View style={{ flexDirection: 'row', justifyContent: 'space-between' }}>
              <Text style={{ color: '#9CA3AF' }}>Membre depuis</Text>
              <Text style={{ color: theme.text, fontWeight: '600' }}>{service.providerStats.memberSince}</Text>
            </View>
          </View>
        </View>

        {/* Boutons d'action */}
        <View style={{ paddingHorizontal: 16, paddingBottom: 32, gap: 12 }}>
          <Pressable style={{
            backgroundColor: '#6d28d9',
            paddingVertical: 16,
            borderRadius: 12,
            alignItems: 'center'
          }}>
            <Text style={{ color: 'white', fontSize: 16, fontWeight: '600' }}>
              Commander maintenant - {service.price}
            </Text>
          </Pressable>

          <Pressable style={{
            borderWidth: 1,
            borderColor: '#6d28d9',
            paddingVertical: 16,
            borderRadius: 12,
            alignItems: 'center'
          }}>
            <Text style={{ color: '#6d28d9', fontSize: 16, fontWeight: '600' }}>
              Contacter le prestataire
            </Text>
          </Pressable>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}
