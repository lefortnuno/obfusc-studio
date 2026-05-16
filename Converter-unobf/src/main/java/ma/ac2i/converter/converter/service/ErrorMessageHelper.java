package ma.ac2i.converter.converter.service;

import org.springframework.dao.DataIntegrityViolationException;

public class ErrorMessageHelper {

    public static String toUserMessage(Exception e) {
        if (e instanceof DataIntegrityViolationException) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("unique index") || msg.contains("unique constraint") || msg.contains("23505")) {
                return "Ce nom est déjà utilisé. Veuillez en choisir un autre.";
            }
            if (msg.contains("not-null") || msg.contains("null value")) {
                return "Un champ obligatoire est manquant.";
            }
            if (msg.contains("foreign key") || msg.contains("referential integrity")) {
                return "Impossible de supprimer : cet élément est utilisé par d'autres données.";
            }
            return "Erreur de données : contrainte de base de données violée.";
        }
        if (e instanceof RuntimeException && e.getMessage() != null
                && !e.getMessage().toLowerCase().contains("hibernate")
                && !e.getMessage().toLowerCase().contains("jdbc")
                && !e.getMessage().toLowerCase().contains("sql")) {
            return e.getMessage();
        }
        String errorCode = Long.toHexString(System.currentTimeMillis()).toUpperCase();
        System.err.println("[ERROR-" + errorCode + "] " + e.getClass().getName() + " - " + e.getMessage());
        return "Une erreur inattendue est survenue (réf. " + errorCode + "). Veuillez consulter les logs ou contacter l'administrateur.";
    }
}
