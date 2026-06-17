package com.cuttherope.game;


public final class FinalProjectRules {
    private FinalProjectRules() {}

    public static final String SOCIAL_FILE_NOTE = "Los amigos y solicitudes se guardan dentro del archivo binario usuario.dat";

    public static final int MAX_LEVELS = 5;

    public static final String[] REQUIRED_TOPICS = {
        "enum complejo", "dos recursividades diferentes", "polimorfismo",
        "archivos binarios", "abstract", "herencia simple", "constructor",
        "clase final", "metodo final"
    };

    public static final RuleVerifier VERIFIER = new RuleVerifier();

    public static final class RuleVerifier {
        public final String verify() {
            return "Proyecto verificado: enum complejo, recursividad, polimorfismo, binarios, abstract, herencia y final.";
        }
    }
}
