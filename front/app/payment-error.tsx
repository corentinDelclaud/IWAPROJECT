import { ThemedText } from "@/components/themed-text";
import { ThemedView } from "@/components/themed-view";
import { Link } from "expo-router";
import { Colors } from "@/constants/theme";
import { useColorScheme } from "@/hooks/use-color-scheme";
import { View } from "react-native";

export default function PaymentError() {
  const colorScheme = useColorScheme() ?? "dark";
  const theme = Colors[colorScheme];

  return (
    <ThemedView style={{ flex: 1, padding: 20 }}>
      <ThemedView style={{ backgroundColor: theme.slateCard, borderRadius: 14, padding: 20, borderWidth: 1, borderColor: theme.slateBorder }}>
        <ThemedText type="title" style={{ textAlign: 'center', marginBottom: 8 }}>Payment failed</ThemedText>
        <ThemedText style={{ textAlign: 'center', color: '#9CA3AF', marginBottom: 12 }}>Something went wrong processing your payment. Please try again or contact support.</ThemedText>
        <View style={{ alignItems: 'center' }}>
          <Link href={'/(tabs)' as any} style={{ display: 'flex' }}>
            <ThemedView style={{ backgroundColor: 'rgba(239,68,68,0.12)', borderColor: 'rgba(239,68,68,0.22)', borderWidth: 1, paddingVertical: 10, paddingHorizontal: 18, borderRadius: 10 }}>
              <ThemedText style={{ color: '#DC2626', fontWeight: '700' }}>Back to home</ThemedText>
            </ThemedView>
          </Link>
        </View>
      </ThemedView>
    </ThemedView>
  );
}
