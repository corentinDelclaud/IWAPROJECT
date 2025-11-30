import React, { useState } from 'react';
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
import { Colors } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { createProduct } from '@/services/productService';

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

    // Log pour déboguer
    console.log('🔍 AddProductModal rendu - visible:', visible, 'userId:', userId);

    const [formData, setFormData] = useState({
        description: '',
        price: '',
        game: '',
        serviceType: '',
    });

    const [showGamePicker, setShowGamePicker] = useState(false);
    const [showServiceTypePicker, setShowServiceTypePicker] = useState(false);

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
            return;
        }

        if (!validateForm()) return;

        try {
            // Préparer les données au format attendu par le backend (CreateProductRequest)
            const productData = {
                description: formData.description.trim(),
                price: parseFloat(formData.price),
                game: formData.game,
                serviceType: formData.serviceType,
                idProvider: userId,
            };

            console.log('Creating product:', productData);

            // Utiliser la fonction createProduct du service
            const createdProduct = await createProduct(productData);

            if (createdProduct) {
                console.log('Product created successfully:', createdProduct);
                Alert.alert('Succès', 'Votre produit a été créé avec succès !');
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
        <Modal
            visible={visible}
            animationType="slide"
            transparent={true}
            onRequestClose={onClose}
        >
            <KeyboardAvoidingView
                behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
                style={styles.modalOverlay}
            >
                <View style={[styles.modalContainer, { backgroundColor: theme.background }]}>
                    <View style={styles.modalHeader}>
                        <Text style={[styles.modalTitle, { color: theme.text }]}>
                            Nouveau Produit
                        </Text>
                        <TouchableOpacity onPress={onClose} style={styles.closeButton}>
                            <Text style={[styles.closeButtonText, { color: theme.text }]}>✕</Text>
                        </TouchableOpacity>
                    </View>

                    <ScrollView style={styles.formContainer} showsVerticalScrollIndicator={false}>

                        {/* Description */}
                        <View style={styles.fieldContainer}>
                            <Text style={[styles.label, { color: theme.text }]}>
                                Description <Text style={styles.required}>*</Text>
                            </Text>
                            <TextInput
                                style={[styles.input, styles.textArea, {
                                    color: theme.text,
                                    backgroundColor: theme.slateCard,
                                    borderColor: colorScheme === 'dark' ? '#374151' : '#e5e7eb'
                                }]}
                                value={formData.description}
                                onChangeText={(text) => setFormData({ ...formData, description: text })}
                                placeholder="Décrivez votre service en détail..."
                                placeholderTextColor={colorScheme === 'dark' ? '#9ca3af' : '#6b7280'}
                                multiline
                                numberOfLines={4}
                                textAlignVertical="top"
                            />
                        </View>

                        {/* Prix */}
                        <View style={styles.fieldContainer}>
                            <Text style={[styles.label, { color: theme.text }]}>
                                Prix (€) <Text style={styles.required}>*</Text>
                            </Text>
                            <TextInput
                                style={[styles.input, {
                                    color: theme.text,
                                    backgroundColor: theme.slateCard,
                                    borderColor: colorScheme === 'dark' ? '#374151' : '#e5e7eb'
                                }]}
                                value={formData.price}
                                onChangeText={(text) => setFormData({ ...formData, price: text })}
                                placeholder="Ex: 30.00"
                                placeholderTextColor={colorScheme === 'dark' ? '#9ca3af' : '#6b7280'}
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
                                style={[styles.button, styles.cancelButton, { backgroundColor: theme.slateCard }]}
                                onPress={() => {
                                    resetForm();
                                    onClose();
                                }}
                            >
                                <Text style={[styles.buttonText, { color: theme.text }]}>Annuler</Text>
                            </TouchableOpacity>

                            <TouchableOpacity
                                style={[
                                    styles.button,
                                    styles.submitButton,
                                    { backgroundColor: !userId ? '#6b7280' : theme.tint }
                                ]}
                                onPress={handleSubmit}
                            >
                                <Text style={[styles.buttonText, { color: '#fff' }]}>
                                    {'Créer'}
                                </Text>
                            </TouchableOpacity>
                        </View>
                    </ScrollView>
                </View>
            </KeyboardAvoidingView>
        </Modal>
    );
}

const styles = StyleSheet.create({
    modalOverlay: {
        flex: 1,
        justifyContent: 'flex-end',
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
    },
    modalContainer: {
        borderTopLeftRadius: 20,
        borderTopRightRadius: 20,
        maxHeight: '90%',
        paddingBottom: 20,
    },
    modalHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: 20,
        borderBottomWidth: 1,
        borderBottomColor: '#e5e7eb',
    },
    modalTitle: {
        fontSize: 24,
        fontWeight: '700',
    },
    closeButton: {
        width: 32,
        height: 32,
        justifyContent: 'center',
        alignItems: 'center',
    },
    closeButtonText: {
        fontSize: 24,
        fontWeight: '300',
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
    },
    required: {
        color: '#ef4444',
    },
    input: {
        borderWidth: 1,
        borderRadius: 12,
        padding: 14,
        fontSize: 16,
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
    },
    pickerButtonText: {
        fontSize: 16,
    },
    pickerOptions: {
        marginTop: 8,
        borderWidth: 1,
        borderRadius: 12,
        overflow: 'hidden',
    },
    pickerOption: {
        padding: 14,
        borderBottomWidth: 1,
        borderBottomColor: '#e5e7eb',
    },
    pickerOptionText: {
        fontSize: 16,
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
        borderColor: '#e5e7eb',
    },
    submitButton: {
        // backgroundColor défini dynamiquement
    },
    buttonText: {
        fontSize: 16,
        fontWeight: '600',
    },
});

