package com.deepan.bettervillagers.villager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class VillagerNameManager {
    private static final String DATA_KEY = "bettervillagers.naming";
    private static final String MODE_KEY = "mode";
    private static final String MODE_GENERATED = "generated";
    private static final String MODE_PLAYER_CUSTOM = "player_custom";
    private static final String BASE_NAME_KEY = "baseName";
    private static final String FIRST_NAME_KEY = "firstName";
    private static final String SURNAME_KEY = "surname";
    private static final String LAST_WRITTEN_NAME_KEY = "lastWrittenName";
    private static final String LAST_HAS_HOME_KEY = "lastHasHome";
    private static final String OBSERVED_NAME_KEY = "observedName";
    private static final String HOBO_PREFIX = "Hobo ";
    private static final int NAME_REFRESH_INTERVAL = 40;
    private static final double BABY_PARENT_SEARCH_RADIUS = 8.0D;

    private static final Map<VillagerType, NamePool> NAME_POOLS = Map.of(
        VillagerType.PLAINS, new NamePool(
            List.of("Aarav", "Aditi", "Arjun", "Diya", "Ishaan", "Kavya", "Rohan", "Mira", "Vivaan", "Anaya", "Kabir", "Priya",
                "Reyansh", "Saanvi", "Dev", "Meera", "Vihaan", "Ira", "Krish", "Tara", "Advik", "Nisha", "Sai", "Lavanya",
                "Yuvan", "Myra", "Dhruv", "Riya"),
            List.of("Patel", "Sharma", "Rao", "Reddy", "Kapoor", "Mehta", "Nair", "Iyer", "Joshi", "Bose", "Verma", "Singh", "Das", "Pillai")
        ),
        VillagerType.DESERT, new NamePool(
            List.of("Zayd", "Layla", "Omar", "Yasmin", "Khalid", "Amira", "Samir", "Noor", "Rafi", "Salma", "Hassan", "Farah",
                "Tariq", "Nadia", "Karim", "Aaliyah", "Idris", "Mariam", "Jamal", "Leila", "Nabil", "Soraya", "Adil", "Zahra",
                "Faris", "Dalia", "Malik", "Rania"),
            List.of("Alim", "Rahman", "Haddad", "Karim", "Nasser", "Qadir", "Samara", "Basir", "Hakim", "Latif", "Mazin", "Sabri", "Tahir", "Younes")
        ),
        VillagerType.SAVANNA, new NamePool(
            List.of("Ayo", "Amahle", "Kofi", "Zuri", "Jelani", "Nia", "Tendai", "Eshe", "Kwame", "Imani", "Thabo", "Amina",
                "Sipho", "Ayana", "Baraka", "Lulu", "Neo", "Makena", "Sefu", "Zola", "Juma", "Adanna", "Omari", "Femi",
                "Tariro", "Penda", "Mandla", "Sade"),
            List.of("Okoye", "Mensah", "Diallo", "Abebe", "Ndlovu", "Kamau", "Biko", "Zuberi", "Afolabi", "Mandela", "Tembo", "Dlamini", "Balewa", "Kenyatta")
        ),
        VillagerType.SNOW, new NamePool(
            List.of("Leif", "Freya", "Soren", "Ingrid", "Bjorn", "Astrid", "Eirik", "Liv", "Anders", "Solveig", "Nils", "Karin",
                "Stellan", "Saga", "Rune", "Maja", "Alvar", "Tove", "Henrik", "Sigrid", "Lars", "Elin", "Magnus", "Asta",
                "Ivar", "Frida", "Knut", "Yrsa"),
            List.of("Halvorsen", "Lind", "Eklund", "Berg", "Nygaard", "Aasen", "Skov", "Dahl", "Torvik", "Rosen", "Vik", "Hagen", "Fjell", "Sund")
        ),
        VillagerType.SWAMP, new NamePool(
            List.of("Adi", "Ayu", "Bima", "Dewi", "Eko", "Fitri", "Gilang", "Intan", "Jaya", "Kartika", "Lukman", "Maya",
                "Nanda", "Putri", "Rizky", "Sari", "Teguh", "Wulan", "Yudi", "Sekar", "Hendra", "Citra", "Bagas", "Ratna",
                "Fajar", "Niken", "Surya", "Rani"),
            List.of("Santoso", "Wijaya", "Saputra", "Permata", "Pratama", "Lestari", "Nugroho", "Kusuma", "Utama", "Wibowo", "Purnama", "Mahendra", "Putra", "Wardani")
        ),
        VillagerType.TAIGA, new NamePool(
            List.of("Aleksei", "Anya", "Dmitri", "Elena", "Ivan", "Katya", "Maksim", "Nadia", "Nikolai", "Olga", "Sergei", "Irina",
                "Yuri", "Svetlana", "Pavel", "Galina", "Mikhail", "Tatyana", "Viktor", "Larisa", "Oleg", "Alina", "Ilya", "Marina",
                "Roman", "Ksenia", "Fyodor", "Polina"),
            List.of("Petrov", "Volkov", "Sokolov", "Morozov", "Ivanov", "Orlov", "Kuznetsov", "Romanov", "Smirnov", "Lebedev", "Mikhailov", "Novikov", "Yegorov", "Belov")
        ),
        VillagerType.JUNGLE, new NamePool(
            List.of("Caio", "Ana", "Bruno", "Lia", "Diego", "Marina", "Enzo", "Bianca", "Felipe", "Clara", "Gabriel", "Luana",
                "Joao", "Isabela", "Lucas", "Taina", "Mateus", "Camila", "Rafael", "Beatriz", "Thiago", "Yara", "Vinicius", "Nina",
                "Pedro", "Larissa", "Gustavo", "Aline"),
            List.of("Silva", "Santos", "Oliveira", "Souza", "Costa", "Pereira", "Almeida", "Lima", "Rocha", "Barbosa", "Cardoso", "Monteiro", "Teixeira", "Moura")
        )
    );

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Villager villager) || !(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (event.loadedFromDisk()) {
            snapshotUnmanagedVillager(villager);
            return;
        }

        if (isManaged(villager)) {
            refreshManagedName(villager);
            return;
        }

        if (villager.hasCustomName()) {
            initializePlayerCustomVillager(villager, getVisibleName(villager));
            return;
        }

        initializeGeneratedVillager(serverLevel, villager);
    }

    @SubscribeEvent
    public void onEntityTickPost(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Villager villager) || !(villager.level() instanceof ServerLevel)) {
            return;
        }

        if (villager.tickCount % NAME_REFRESH_INTERVAL != 0) {
            return;
        }

        if (isManaged(villager)) {
            handleManagedVillagerTick(villager);
        } else {
            handleUnmanagedVillagerTick(villager);
        }
    }

    private void initializeGeneratedVillager(ServerLevel level, Villager villager) {
        NamePool pool = getNamePool(villager);
        String firstName = pickRandom(pool.firstNames(), villager);
        String surname = pickRandom(pool.surnames(), villager);
        List<Villager> parents = villager.isBaby() ? findCandidateParents(level, villager) : List.of();
        String baseName = villager.isBaby()
            ? determineBabyBaseName(villager, firstName, surname, parents)
            : buildBaseName(firstName, surname);

        CompoundTag data = getNamingData(villager);
        data.putString(MODE_KEY, MODE_GENERATED);
        data.putString(FIRST_NAME_KEY, firstName);
        data.putString(SURNAME_KEY, extractSurname(baseName));
        data.putString(BASE_NAME_KEY, baseName);
        snapshotObservedName(data, baseName);

        if (villager.isBaby()) {
            VillagerGenealogySavedData.get(level).ensureVillagerRecord(villager, baseName, parents);
        } else {
            VillagerGenealogySavedData.get(level).ensureVillagerRecord(villager, baseName);
        }

        applyManagedName(villager, baseName);
    }

    private void initializePlayerCustomVillager(Villager villager, String currentName) {
        String baseName = stripHoboPrefix(currentName);
        CompoundTag data = getNamingData(villager);
        data.putString(MODE_KEY, MODE_PLAYER_CUSTOM);
        data.putString(BASE_NAME_KEY, baseName);
        data.remove(FIRST_NAME_KEY);
        data.putString(SURNAME_KEY, extractSurname(baseName));
        snapshotObservedName(data, baseName);

        if (villager.level() instanceof ServerLevel serverLevel) {
            VillagerGenealogySavedData.get(serverLevel).ensureVillagerRecord(villager, baseName);
        }

        applyManagedName(villager, baseName);
    }

    private void handleManagedVillagerTick(Villager villager) {
        CompoundTag data = getNamingData(villager);
        String currentVisibleName = getVisibleName(villager);
        String lastWrittenName = data.getString(LAST_WRITTEN_NAME_KEY);

        if (currentVisibleName.isBlank()) {
            refreshManagedName(villager);
            return;
        }

        if (!currentVisibleName.equals(lastWrittenName)) {
            initializePlayerCustomVillager(villager, currentVisibleName);
            return;
        }

        refreshManagedName(villager);
    }

    private void handleUnmanagedVillagerTick(Villager villager) {
        CompoundTag data = getNamingData(villager);
        String currentVisibleName = getVisibleName(villager);
        String observedName = data.getString(OBSERVED_NAME_KEY);

        if (!currentVisibleName.equals(observedName)) {
            if (currentVisibleName.isBlank()) {
                snapshotObservedName(data, "");
                return;
            }

            initializePlayerCustomVillager(villager, currentVisibleName);
        }
    }

    private void refreshManagedName(Villager villager) {
        CompoundTag data = getNamingData(villager);
        String baseName = data.getString(BASE_NAME_KEY);
        if (baseName.isBlank()) {
            return;
        }

        boolean hasHome = hasAssignedHome(villager);
        boolean knownHome = !data.contains(LAST_HAS_HOME_KEY) || data.getBoolean(LAST_HAS_HOME_KEY);
        if (knownHome == hasHome && getVisibleName(villager).equals(data.getString(LAST_WRITTEN_NAME_KEY))) {
            return;
        }

        String displayName = formatDisplayName(baseName, hasHome);
        villager.setCustomName(Component.literal(displayName));
        data.putString(LAST_WRITTEN_NAME_KEY, displayName);
        data.putBoolean(LAST_HAS_HOME_KEY, hasHome);
    }

    private void snapshotUnmanagedVillager(Villager villager) {
        if (isManaged(villager)) {
            return;
        }

        snapshotObservedName(getNamingData(villager), getVisibleName(villager));
    }

    private void applyManagedName(Villager villager, String baseName) {
        CompoundTag data = getNamingData(villager);
        String displayName = formatDisplayName(baseName, hasAssignedHome(villager));
        villager.setCustomName(Component.literal(displayName));
        data.putString(LAST_WRITTEN_NAME_KEY, displayName);
        data.putBoolean(LAST_HAS_HOME_KEY, hasAssignedHome(villager));
    }

    private String determineBabyBaseName(Villager baby, String firstName, String fallbackSurname, List<Villager> parents) {
        String inheritedSurname = determineInheritedSurname(parents, baby, fallbackSurname);
        ParentNameContext chosenParent = chooseParentContext(parents, baby);

        return switch (baby.getRandom().nextInt(4)) {
            case 0 -> buildBaseName(firstName, inheritedSurname);
            case 1 -> buildParentSonName(firstName, chosenParent, inheritedSurname);
            case 2 -> buildJuniorName(chosenParent, firstName, inheritedSurname);
            default -> buildInitialedName(firstName, inheritedSurname, parents);
        };
    }

    private List<Villager> findCandidateParents(ServerLevel level, Villager baby) {
        List<Villager> parents = level.getEntitiesOfClass(Villager.class, getParentSearchBox(baby), candidate ->
            candidate != baby && !candidate.isBaby() && candidate.distanceToSqr(baby) <= BABY_PARENT_SEARCH_RADIUS * BABY_PARENT_SEARCH_RADIUS
        );

        parents.sort(
            Comparator.<Villager>comparingInt(parent -> parent.getAge() > 0 ? 0 : 1)
                .thenComparingDouble(parent -> parent.distanceToSqr(baby))
        );

        return parents.stream()
            .limit(2)
            .toList();
    }

    private String determineInheritedSurname(List<Villager> parents, Villager baby, String fallbackSurname) {
        List<String> surnames = parents.stream().map(this::getParentSurname).filter(name -> !name.isBlank()).toList();

        if (!surnames.isEmpty()) {
            return surnames.get(baby.getRandom().nextInt(surnames.size()));
        }

        return fallbackSurname;
    }

    private ParentNameContext chooseParentContext(List<Villager> parents, Villager baby) {
        List<ParentNameContext> contexts = parents.stream()
            .map(this::buildParentContext)
            .filter(context -> !context.baseName().isBlank())
            .toList();

        if (!contexts.isEmpty()) {
            return contexts.get(baby.getRandom().nextInt(contexts.size()));
        }

        return new ParentNameContext("", "", "");
    }

    private String getParentSurname(Villager villager) {
        CompoundTag data = getNamingData(villager);
        if (isManaged(villager) && data.contains(SURNAME_KEY)) {
            String storedSurname = data.getString(SURNAME_KEY).trim();
            if (!storedSurname.isBlank()) {
                return storedSurname;
            }
        }

        String nameSource = isManaged(villager) ? data.getString(BASE_NAME_KEY) : getVisibleName(villager);
        return extractSurname(stripHoboPrefix(nameSource));
    }

    private ParentNameContext buildParentContext(Villager villager) {
        String baseName = isManaged(villager) ? getNamingData(villager).getString(BASE_NAME_KEY) : getVisibleName(villager);
        String cleanedBaseName = stripHoboPrefix(baseName).trim();
        String firstToken = extractFirstToken(cleanedBaseName);
        String surname = extractSurname(cleanedBaseName);
        return new ParentNameContext(cleanedBaseName, firstToken, surname);
    }

    private String buildParentSonName(String firstName, ParentNameContext parent, String fallbackSurname) {
        String parentToken = !parent.firstToken().isBlank() ? parent.firstToken() : fallbackSurname;
        return firstName + " " + parentToken + "son";
    }

    private String buildJuniorName(ParentNameContext parent, String firstName, String surname) {
        String parentBaseName = parent.baseName();
        return parentBaseName.isBlank() ? buildBaseName(firstName, surname) : parentBaseName + " Jr.";
    }

    private String buildInitialedName(String firstName, String surname, List<Villager> parents) {
        String initials = parents.stream()
            .map(this::buildParentContext)
            .map(ParentNameContext::firstToken)
            .filter(token -> !token.isBlank())
            .limit(2)
            .map(token -> token.substring(0, 1).toUpperCase())
            .reduce("", String::concat);

        if (initials.isBlank()) {
            return buildBaseName(firstName, surname);
        }

        return firstName + " " + initials + " " + surname;
    }

    private AABB getParentSearchBox(Villager baby) {
        return baby.getBoundingBox().inflate(BABY_PARENT_SEARCH_RADIUS, 4.0D, BABY_PARENT_SEARCH_RADIUS);
    }

    private boolean hasAssignedHome(Villager villager) {
        return villager.getBrain().hasMemoryValue(MemoryModuleType.HOME);
    }

    private NamePool getNamePool(Villager villager) {
        return NAME_POOLS.getOrDefault(villager.getVillagerData().getType(), NAME_POOLS.get(VillagerType.PLAINS));
    }

    private String pickRandom(List<String> values, Villager villager) {
        return values.get(villager.getRandom().nextInt(values.size()));
    }

    private String buildBaseName(String firstName, String surname) {
        return firstName + " " + surname;
    }

    private String formatDisplayName(String baseName, boolean hasHome) {
        return hasHome ? baseName : HOBO_PREFIX + baseName;
    }

    private String extractSurname(String baseName) {
        String trimmed = baseName.trim();
        if (trimmed.isBlank()) {
            return "";
        }

        int splitIndex = trimmed.lastIndexOf(' ');
        return splitIndex >= 0 ? trimmed.substring(splitIndex + 1) : trimmed;
    }

    private String extractFirstToken(String baseName) {
        String trimmed = baseName.trim();
        if (trimmed.isBlank()) {
            return "";
        }

        int splitIndex = trimmed.indexOf(' ');
        return splitIndex >= 0 ? trimmed.substring(0, splitIndex) : trimmed;
    }

    private String stripHoboPrefix(String name) {
        String trimmed = name.trim();
        return trimmed.startsWith(HOBO_PREFIX) ? trimmed.substring(HOBO_PREFIX.length()).trim() : trimmed;
    }

    private String getVisibleName(Villager villager) {
        return villager.hasCustomName() ? villager.getCustomName().getString() : "";
    }

    private boolean isManaged(Villager villager) {
        CompoundTag root = villager.getPersistentData();
        return root.contains(DATA_KEY, CompoundTag.TAG_COMPOUND) && root.getCompound(DATA_KEY).contains(MODE_KEY);
    }

    private CompoundTag getNamingData(Villager villager) {
        CompoundTag root = villager.getPersistentData();
        if (!root.contains(DATA_KEY, CompoundTag.TAG_COMPOUND)) {
            root.put(DATA_KEY, new CompoundTag());
        }

        return root.getCompound(DATA_KEY);
    }

    private void snapshotObservedName(CompoundTag data, String observedName) {
        data.putString(OBSERVED_NAME_KEY, observedName);
    }

    private record NamePool(List<String> firstNames, List<String> surnames) {
    }

    private record ParentNameContext(String baseName, String firstToken, String surname) {
    }
}
