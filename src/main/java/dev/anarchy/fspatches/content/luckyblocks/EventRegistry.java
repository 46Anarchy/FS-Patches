package dev.anarchy.fspatches.content.luckyblocks;

import fr.paladium.palamod.modules.luckyblock.utils.ALuckyEvent;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.reflections.Reflections;

import java.util.*;

@UtilityClass
public class EventRegistry {

    private static final HashMap<Class<? extends ALuckyEvent>, Class<? extends AbstractCustomLuckyBlock>[]> eventRegistry = new HashMap<>();
    private static boolean init = false;

    public static void init() {
        if (init)
            throw new RuntimeException("Trying to initialize the ALuckyEvent registry twice !");

        System.out.println("Looking for events in package \"" + EventRegistry.class.getPackage().getName() + ".events\"");

        new Reflections(EventRegistry.class.getPackage().getName() + ".events")
                .getTypesAnnotatedWith(EventRegister.class)
                .stream().filter(cl -> ALuckyEvent.class.isAssignableFrom(cl))
                .forEach(cl -> {
                    System.out.println("Registering event " + cl.getName());
                    EventRegister register = cl.getAnnotation(EventRegister.class);
                    registerEvent((Class<? extends ALuckyEvent>) cl, register.value());
                });

        init = true;
    }

    public static void registerEvent(Class<? extends ALuckyEvent> event, Class<? extends AbstractCustomLuckyBlock>[] types) {
        Set<Class<? extends AbstractCustomLuckyBlock>> set = new HashSet<>();
        set.addAll(Arrays.asList(types));
        if (eventRegistry.containsKey(event)) {
            set.addAll(Arrays.asList(eventRegistry.get(event)));
            eventRegistry.replace(event, set.toArray(new Class[0]));
            return;
        }
        eventRegistry.put(event, set.toArray(new Class[0]));
    }

    public static void registerEvent(Class<? extends ALuckyEvent> event, Class<? extends AbstractCustomLuckyBlock> type) {
        registerEvent(event, new Class[]{type});
    }

    public static List<ALuckyEvent> getEventsForType(Class<? extends AbstractCustomLuckyBlock> type) {
        List<ALuckyEvent> events = new ArrayList<>();

        eventRegistry.forEach((key, value) -> {
            try {
                events.add(key.newInstance());
            } catch (Exception ignored) {
            }
        });

        return events;
    }

    @SneakyThrows
    public static ALuckyEvent getEventByClass(String event) {
        Class<? extends ALuckyEvent> clazz = eventRegistry.keySet().stream().filter(cl -> cl.getName().equalsIgnoreCase(event)).findFirst().orElse(null);
        if (clazz == null)
            return null;

        return clazz.newInstance();
    }
}
