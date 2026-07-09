package it.unical.ea.enums;

/**
 * Visibilità di una lista di itinerari preferiti.
 */
public enum FavoriteListVisibility {
    /** Visibile soltanto al proprietario. */
    PRIVATE,
    /** Visibile al proprietario e agli utenti esplicitamente autorizzati. */
    SHARED,
    /** Visibile a chiunque possieda il link di condivisione, anche senza autenticazione. */
    PUBLIC
}
