# Ticket Sénégal

Plateforme de vente et de contrôle de tickets, avec frontend statique déployable sur Vercel et API serverless Supabase.

## Déploiement Vercel + Supabase

1. Créez un projet Supabase et exécutez `supabase/schema.sql` dans le SQL Editor.
2. Créez un utilisateur administrateur dans Supabase Auth, puis ajoutez son profil dans `profiles` avec `role = 'ADMIN'` et une adresse email identique.
3. Importez le projet dans Vercel depuis la racine du dépôt.
4. Ajoutez les variables Vercel `SUPABASE_URL`, `SUPABASE_SERVICE_ROLE_KEY`, `TESTING_MODE=false` et, si nécessaire, `FRONTEND_ORIGIN`.
5. Déployez. Vercel sert les pages de `frontend/` et les fonctions de `api/`.

La clé `SUPABASE_SERVICE_ROLE_KEY` est strictement réservée à Vercel et ne doit jamais être exposée au navigateur.

## Démarrage

1. Installez **JDK 17+**, Maven et démarrez MySQL (XAMPP).
2. Importez le schéma : `mysql -u root -p < database/database.sql`
3. Lancez l’API (obligatoire avant tout achat) : `cd backend && mvn spring-boot:run`
4. Dans un autre terminal, servez le frontend : `cd frontend && python3 -m http.server 5500`
5. Ouvrez `http://localhost:5500`.

Configuration possible : `DB_URL`, `DB_USER`, `DB_PASSWORD` et `PORT`. Exemple : `DB_PASSWORD=monmotdepasse mvn spring-boot:run`.

Si la page affiche « serveur de tickets indisponible » ou « Load failed », le backend sur `http://localhost:8080` n’est pas lancé. Installez Java 17+ et Maven si nécessaire, démarrez MySQL, puis lancez exactement la commande de l’étape 3 dans un terminal séparé. XAMPP ne lance que les fichiers HTML/PHP : il ne démarre pas Spring Boot.

Compte administrateur de démonstration : `admin@tickets.local` / `Admin2026!` (changez-le avant production).

## Règles importantes

- Le serveur est la source de vérité : les achats sont autorisés uniquement du jeudi 00:00 au vendredi 10:00, heure `Africa/Dakar`.
- Chaque billet est créé individuellement, possède un jeton QR secret, et expire exactement à `purchasedAt.plusMonths(1)`.
- La vérification marque immédiatement un billet valide comme `USED`; un second passage est refusé et journalisé.
- Les prix sont lus depuis MySQL et jamais acceptés depuis le navigateur.

En production, remplacez le paiement simulé (`PAID`) par un prestataire, servez le frontend sous HTTPS, utilisez une clé/session persistante et changez le compte initial.
