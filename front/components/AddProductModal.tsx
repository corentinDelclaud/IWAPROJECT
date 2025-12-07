import React, { useState, useEffect } from 'react';
import {
    View,
    Text,
    Modal,
    TextInput,
    TouchableOpacity,
    ScrollView,
    StyleSheet,
    Alert,
    KeyboardAvoidingView,
    Platform,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Colors } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { createProduct } from '@/services/productService';
import { apiService } from '@/services/api';
import { StripeOnboardingModal } from './StripeOnboardingModal';

interface AddProductModalProps {
    visible: boolean;
    onClose: () => void;
    onProductAdded: () => void;
    userId: string;
}

// Options pour les jeux et types de services (doivent correspondre aux enums du backend)
const GAME_OPTIONS = [
    { label: 'League of Legends', value: 'LEAGUE_OF_LEGENDS' },
    { label: 'Valorant', value: 'VALORANT' },
    { label: 'Rocket League', value: 'ROCKET_LEAGUE' },
    { label: 'Teamfight Tactics', value: 'TEAMFIGHT_TACTICS' },
    { label: 'Autre', value: 'OTHER' },
];

const SERVICE_TYPE_OPTIONS = [
    { label: 'Boost', value: 'BOOST' },
    { label: 'Coaching', value: 'COACHING' },
    { label: 'Revente de compte', value: 'ACCOUNT_RESALING' },
    { label: 'Autre', value: 'OTHER' },
];

export default function AddProductModal({
    visible,
    onClose,
    onProductAdded,
    userId,
}: AddProductModalProps) {
    const colorScheme = useColorScheme() ?? 'light';
    const theme = Colors[colorScheme];


    const [formData, setFormData] = useState({
        description: '',
        price: '',
        game: '',
        serviceType: '',
    });

    const [showGamePicker, setShowGamePicker] = useState(false);
    const [showServiceTypePicker, setShowServiceTypePicker] = useState(false);
    const [stripeModalVisible, setStripeModalVisible] = useState(false);
    const [stripeOnboardingUrl, setStripeOnboardingUrl] = useState<string | null>(null);
    const [isCheckingStripe, setIsCheckingStripe] = useState(false);
    const [stripeCheckDone, setStripeCheckDone] = useState(false);
    const [hasOnboardingUrl, setHasOnboardingUrl] = useState(false); // Pour savoir si on a déjà un lien d'onboarding

    // Vérifier le compte Stripe dès que le modal devient visible
    useEffect(() => {
        if (visible && !stripeCheckDone && !isCheckingStripe) {
            checkStripeAccount();
        }
        // Reset quand le modal se ferme
        if (!visible) {
            setStripeCheckDone(false);
            setHasOnboardingUrl(false); // Reset aussi le flag du lien
        }
    }, [visible]);

    const checkStripeAccount = async () => {
        if (!userId) {
            Alert.alert('Erreur', 'Veuillez patienter, chargement de votre profil en cours...');
            onClose();
            return;
        }

        setIsCheckingStripe(true);

        try {
            const profile = await apiService.getUserProfile();
            const stripeAccountId = profile?.stripeAccountId;

            const fetchOnboardingLink = async (accountId: string) => {
                try {
                    const resp = await apiService.createStripeAccountLink(accountId);
                    const url = resp?.url;
                    if (url) {
                        setStripeOnboardingUrl(url);
                        setHasOnboardingUrl(true); // Marquer qu'on a un lien
                        // Attendre un peu avant d'ouvrir la modal Stripe pour que le modal produit se ferme d'abord
                        setTimeout(() => {
                            setStripeModalVisible(true);
                        }, 100);
                    } else {
                        Alert.alert('Erreur', "Impossible d'obtenir le lien d'onboarding Stripe.");
                    }
                } catch (err) {
                    console.error('Failed to create account link:', err);
                    Alert.alert('Erreur', "Impossible de créer le lien d'onboarding Stripe.");
                } finally {
                    setIsCheckingStripe(false);
                }
            };

            if (!stripeAccountId) {
                // Si on a déjà un lien d'onboarding, on réouvre juste la modal
                if (hasOnboardingUrl && stripeOnboardingUrl) {
                    setIsCheckingStripe(false);
                    setTimeout(() => {
                        setStripeModalVisible(true);
                    }, 100);
                    return;
                }

                // Proposer de créer un compte Stripe
                setIsCheckingStripe(false);
                Alert.alert(
                    'Compte Stripe manquant',
                    "Votre compte Stripe n'est pas encore connecté. Voulez-vous créer un compte lié et démarrer l'onboarding ?",
                    [
                        { text: 'Annuler', style: 'cancel' },
                        {
                            text: "Créer et connecter",
                            onPress: async () => {
                                try {
                                    if (!profile?.email) {
                                        Alert.alert('Erreur', "Adresse e-mail manquante pour créer le compte Stripe.");
                                        return;
                                    }
                                    const createResp = await apiService.createConnectAccount(profile.email);
                                    const newAccountId = createResp?.accountId;
                                    if (newAccountId) {
                                        await fetchOnboardingLink(newAccountId);
                                    } else {
                                        Alert.alert('Erreur', "Impossible de créer le compte Stripe.");
                                    }
                                } catch (err) {
                                    console.error('Failed to create connect account:', err);
                                    Alert.alert('Erreur', "Impossible de créer le compte Stripe. Réessayez plus tard.");
                                }
                            },
                        },
                    ]
                );
                return;
            }

            // Vérifier le statut du compte
            const status = await apiService.getStripeAccountStatus(stripeAccountId);
            if (!status || !(status.payoutsEnabled && status.chargesEnabled && status.detailsSubmitted)) {
                // Si on a déjà un lien d'onboarding, on réouvre juste la modal
                if (hasOnboardingUrl && stripeOnboardingUrl) {
                    setIsCheckingStripe(false);
                    setTimeout(() => {
                        setStripeModalVisible(true);
                    }, 100);
                    return;
                }
                
                await fetchOnboardingLink(stripeAccountId);
                return;
            }

            // Compte Stripe OK, on peut continuer avec le formulaire
            setStripeCheckDone(true);
        } catch (err) {
            console.error('Failed to verify Stripe account status:', err);
            Alert.alert('Erreur', "Impossible de vérifier l'état de votre compte Stripe. Réessayez ultérieurement.");
            onClose();
        } finally {
            setIsCheckingStripe(false);
        }
    };

    const resetForm = () => {
        setFormData({
            description: '',
            price: '',
            game: '',
            serviceType: '',
        });
    };

    const validateForm = () => {
        if (!formData.description.trim()) {
            Alert.alert('Erreur', 'La description est obligatoire');
            return false;
        }
        if (!formData.price || isNaN(parseFloat(formData.price)) || parseFloat(formData.price) <= 0) {
            Alert.alert('Erreur', 'Veuillez entrer un prix valide supérieur à 0');
            return false;
        }
        if (!formData.game) {
            Alert.alert('Erreur', 'Veuillez sélectionner un jeu');
            return false;
        }
        if (!formData.serviceType) {
            Alert.alert('Erreur', 'Veuillez sélectionner un type de service');
            return false;
        }
        return true;
    };

    const handleSubmit = async () => {
        // Vérifier que userId est défini
        if (!userId) {
            Alert.alert('Erreur', 'Veuillez patienter, chargement de votre profil en cours...');
            // retry to fetch user
            try {
                const profile = await apiService.getUserProfile();
                if (profile?.id) {
                    // Retry submit with fetched userId
                    return handleSubmit();
                }
            } catch (error) {
                console.error('Failed to fetch user profile:', error);
            }
            return;
        }

        if (!validateForm()) return;

        try {
            // Récupérer le stripeAccountId du profil
            const profile = await apiService.getUserProfile();
            const stripeAccountId = profile?.stripeAccountId;

            if (!stripeAccountId) {
                Alert.alert(
                    'Erreur',
                    'Votre compte Stripe n\'est pas configuré. Veuillez compléter l\'onboarding Stripe avant de créer un produit.'
                );
                return;
            }

            // Préparer les données au format attendu par le backend (CreateProductRequest)
            const productData = {
                description: formData.description.trim(),
                price: parseFloat(formData.price),
                game: formData.game,
                serviceType: formData.serviceType,
                idProvider: userId,
                stripeAccountId: stripeAccountId, // Ajouter le stripeAccountId
            };

            console.log('Creating product:', productData);

            // Utiliser la fonction createProduct du service
            const createdProduct = await createProduct(productData);

            if (createdProduct) {
                console.log('Product created successfully:', createdProduct);
                Alert.alert('Succès', 'Votre produit a été créé avec succès dans votre catalogue et sur Stripe !');
                resetForm();
                onClose();
                onProductAdded();
            } else {
                throw new Error('Failed to create product');
            }
        } catch (error) {
            console.error('Error creating product:', error);
            Alert.alert('Erreur', 'Impossible de créer le produit. Veuillez réessayer.');
        }
    };

    const getGameLabel = (value: string) => {
        return GAME_OPTIONS.find(opt => opt.value === value)?.label || 'Sélectionner un jeu';
    };

    const getServiceTypeLabel = (value: string) => {
        return SERVICE_TYPE_OPTIONS.find(opt => opt.value === value)?.label || 'Sélectionner un type';
    };

    return (
        <>
            <Modal
                visible={visible && stripeCheckDone}
                animationType="slide"
                transparent={true}
                onRequestClose={onClose}
            >
                <KeyboardAvoidingView
                behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
                style={styles.modalOverlay}
            >
                <LinearGradient
                    colors={["#0f172c", "#401c87", "#1e293b"]}
                    locations={[0, 0.5, 1]}
                    start={{ x: 0, y: 0 }}
                    end={{ x: 1, y: 1 }}
                    style={styles.modalContainer}
                >
                    <View style={styles.modalHeader}>
                        <Text style={styles.modalTitle}>
                            ✨ Nouveau Produit
                        </Text>
                        <TouchableOpacity onPress={onClose} style={styles.closeButton}>
                            <Text style={styles.closeButtonText}>×</Text>
                        </TouchableOpacity>
                    </View>

                    <ScrollView style={styles.formContainer} showsVerticalScrollIndicator={false}>

                        {/* Description */}
                        <View style={styles.fieldContainer}>
                            <Text style={styles.label}>
                                Description <Text style={styles.required}>*</Text>
                            </Text>
                            <TextInput
                                style={[styles.input, styles.textArea]}
                                value={formData.description}
                                onChangeText={(text) => setFormData({ ...formData, description: text })}
                                placeholder="Décrivez votre service en détail..."
                                placeholderTextColor="#9ca3af"
                                multiline
                                numberOfLines={4}
                                textAlignVertical="top"
                            />
                        </View>

                        {/* Prix */}
                        <View style={styles.fieldContainer}>
                            <Text style={styles.label}>
                                Prix (€) <Text style={styles.required}>*</Text>
                            </Text>
                            <TextInput
                                style={styles.input}
                                value={formData.price}
                                onChangeText={(text) => setFormData({ ...formData, price: text })}
                                placeholder="Ex: 30.00"
                                placeholderTextColor="#9ca3af"
                                keyboardType="decimal-pad"
                            />
                        </View>

                        {/* Jeu */}
                        <View style={styles.fieldContainer}>
                            <Text style={[styles.label, { color: theme.text }]}>
                                Jeu <Text style={styles.required}>*</Text>
                            </Text>
                            <TouchableOpacity
                                style={[styles.pickerButton, {
                                    backgroundColor: theme.slateCard,
                                    borderColor: colorScheme === 'dark' ? '#374151' : '#e5e7eb'
                                }]}
                                onPress={() => setShowGamePicker(!showGamePicker)}
                            >
                                <Text style={[styles.pickerButtonText, {
                                    color: formData.game ? theme.text : (colorScheme === 'dark' ? '#9ca3af' : '#6b7280')
                                }]}>
                                    {getGameLabel(formData.game)}
                                </Text>
                                <Text style={{ color: theme.text }}>▼</Text>
                            </TouchableOpacity>
                            {showGamePicker && (
                                <View style={[styles.pickerOptions, {
                                    backgroundColor: theme.slateCard,
                                    borderColor: colorScheme === 'dark' ? '#374151' : '#e5e7eb'
                                }]}>
                                    {GAME_OPTIONS.map((option) => (
                                        <TouchableOpacity
                                            key={option.value}
                                            style={[styles.pickerOption, {
                                                backgroundColor: formData.game === option.value
                                                    ? theme.tint + '20'
                                                    : 'transparent'
                                            }]}
                                            onPress={() => {
                                                setFormData({ ...formData, game: option.value });
                                                setShowGamePicker(false);
                                            }}
                                        >
                                            <Text style={[styles.pickerOptionText, {
                                                color: formData.game === option.value ? theme.tint : theme.text
                                            }]}>
                                                {option.label}
                                            </Text>
                                        </TouchableOpacity>
                                    ))}
                                </View>
                            )}
                        </View>

                        {/* Type de service */}
                        <View style={styles.fieldContainer}>
                            <Text style={[styles.label, { color: theme.text }]}>
                                Type de service <Text style={styles.required}>*</Text>
                            </Text>
                            <TouchableOpacity
                                style={[styles.pickerButton, {
                                    backgroundColor: theme.slateCard,
                                    borderColor: colorScheme === 'dark' ? '#374151' : '#e5e7eb'
                                }]}
                                onPress={() => setShowServiceTypePicker(!showServiceTypePicker)}
                            >
                                <Text style={[styles.pickerButtonText, {
                                    color: formData.serviceType ? theme.text : (colorScheme === 'dark' ? '#9ca3af' : '#6b7280')
                                }]}>
                                    {getServiceTypeLabel(formData.serviceType)}
                                </Text>
                                <Text style={{ color: theme.text }}>▼</Text>
                            </TouchableOpacity>
                            {showServiceTypePicker && (
                                <View style={[styles.pickerOptions, {
                                    backgroundColor: theme.slateCard,
                                    borderColor: colorScheme === 'dark' ? '#374151' : '#e5e7eb'
                                }]}>
                                    {SERVICE_TYPE_OPTIONS.map((option) => (
                                        <TouchableOpacity
                                            key={option.value}
                                            style={[styles.pickerOption, {
                                                backgroundColor: formData.serviceType === option.value
                                                    ? theme.tint + '20'
                                                    : 'transparent'
                                            }]}
                                            onPress={() => {
                                                setFormData({ ...formData, serviceType: option.value });
                                                setShowServiceTypePicker(false);
                                            }}
                                        >
                                            <Text style={[styles.pickerOptionText, {
                                                color: formData.serviceType === option.value ? theme.tint : theme.text
                                            }]}>
                                                {option.label}
                                            </Text>
                                        </TouchableOpacity>
                                    ))}
                                </View>
                            )}
                        </View>

                        {/* Boutons */}
                        <View style={styles.buttonContainer}>
                            <TouchableOpacity
                                style={[styles.button, styles.cancelButton]}
                                onPress={() => {
                                    resetForm();
                                    onClose();
                                }}
                            >
                                <Text style={[styles.buttonText, { color: '#ECEDEE' }]}>Annuler</Text>
                            </TouchableOpacity>

                            <TouchableOpacity
                                style={[
                                    styles.button,
                                    styles.submitButton,
                                    { backgroundColor: !userId ? '#6b7280' : theme.purple }
                                ]}
                                onPress={handleSubmit}
                            >
                                <Text style={[styles.buttonText, { color: '#fff' }]}>
                                    ✨ Créer
                                </Text>
                            </TouchableOpacity>
                        </View>
                    </ScrollView>
                </LinearGradient>
            </KeyboardAvoidingView>
            </Modal>

            <StripeOnboardingModal
                visible={stripeModalVisible}
                url={stripeOnboardingUrl}
                onClose={() => setStripeModalVisible(false)}
                onRecheck={async () => {
                    // Fermer la modal Stripe
                    setStripeModalVisible(false);
                    // Relancer la vérification du compte
                    setStripeCheckDone(false);
                    await checkStripeAccount();
                }}
            />
        </>
    );
}

const styles = StyleSheet.create({
    modalOverlay: {
        flex: 1,
        justifyContent: 'flex-end',
        backgroundColor: 'rgba(15, 23, 44, 0.95)',
    },
    modalContainer: {
        borderTopLeftRadius: 24,
        borderTopRightRadius: 24,
        maxHeight: '90%',
        paddingBottom: 20,
        borderTopWidth: 2,
        borderTopColor: 'rgba(139, 92, 246, 0.3)',
    },
    modalHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: 20,
        borderBottomWidth: 1,
        borderBottomColor: 'rgba(139, 92, 246, 0.2)',
    },
    modalTitle: {
        fontSize: 26,
        fontWeight: '700',
        color: '#ECEDEE',
        letterSpacing: 0.5,
    },
    closeButton: {
        width: 36,
        height: 36,
        borderRadius: 18,
        backgroundColor: 'rgba(139, 92, 246, 0.2)',
        justifyContent: 'center',
        alignItems: 'center',
    },
    closeButtonText: {
        fontSize: 28,
        fontWeight: '300',
        color: '#ECEDEE',
    },
    formContainer: {
        padding: 20,
    },
    fieldContainer: {
        marginBottom: 20,
    },
    label: {
        fontSize: 16,
        fontWeight: '600',
        marginBottom: 8,
        color: '#ECEDEE',
    },
    required: {
        color: '#f87171',
    },
    input: {
        borderWidth: 1,
        borderRadius: 12,
        padding: 14,
        fontSize: 16,
        backgroundColor: 'rgba(30, 41, 59, 0.4)',
        borderColor: 'rgba(139, 92, 246, 0.3)',
        color: '#ECEDEE',
    },
    textArea: {
        height: 100,
        paddingTop: 14,
    },
    pickerButton: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        borderWidth: 1,
        borderRadius: 12,
        padding: 14,
        backgroundColor: 'rgba(30, 41, 59, 0.4)',
        borderColor: 'rgba(139, 92, 246, 0.3)',
    },
    pickerButtonText: {
        fontSize: 16,
        color: '#ECEDEE',
    },
    pickerOptions: {
        marginTop: 8,
        borderWidth: 1,
        borderRadius: 12,
        overflow: 'hidden',
        backgroundColor: 'rgba(15, 23, 44, 0.95)',
        borderColor: 'rgba(139, 92, 246, 0.3)',
    },
    pickerOption: {
        padding: 14,
        borderBottomWidth: 1,
        borderBottomColor: 'rgba(139, 92, 246, 0.2)',
    },
    pickerOptionText: {
        fontSize: 16,
        color: '#ECEDEE',
    },
    buttonContainer: {
        flexDirection: 'row',
        gap: 12,
        marginTop: 20,
    },
    button: {
        flex: 1,
        padding: 16,
        borderRadius: 12,
        alignItems: 'center',
    },
    cancelButton: {
        borderWidth: 1,
        borderColor: 'rgba(139, 92, 246, 0.5)',
        backgroundColor: 'rgba(30, 41, 59, 0.4)',
    },
    submitButton: {
        // backgroundColor défini dynamiquement
        shadowColor: '#8b5cf6',
        shadowOffset: { width: 0, height: 4 },
        shadowOpacity: 0.3,
        shadowRadius: 8,
        elevation: 5,
    },
    buttonText: {
        fontSize: 16,
        fontWeight: '600',
    },
});

