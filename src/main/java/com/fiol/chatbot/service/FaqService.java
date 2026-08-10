package com.fiol.chatbot.service;

import com.fiol.chatbot.dto.FaqEntry;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FaqService {

    private static final List<FaqEntry> ENTRIES = List.of(
            new FaqEntry("¿Cuáles son los horarios de atención?",
                    "Atendemos de lunes a viernes de 9 a 18 hs y sábados de 9 a 13 hs."),
            new FaqEntry("¿Hacen envíos?",
                    "Sí, hacemos envíos a todo el país por correo o cadetería en CABA/GBA."),
            new FaqEntry("¿Qué medios de pago aceptan?",
                    "Efectivo, transferencia y tarjetas de débito/crédito."),
            new FaqEntry("¿Tienen local físico?",
                    "Sí, podés visitarnos. Elegí \"Hablar con humano\" para coordinar una visita."));

    public String renderAll() {
        StringBuilder sb = new StringBuilder("Preguntas frecuentes:\n\n");
        for (int i = 0; i < ENTRIES.size(); i++) {
            FaqEntry entry = ENTRIES.get(i);
            sb.append(i + 1).append(") ").append(entry.question()).append("\n")
                    .append(entry.answer()).append("\n\n");
        }
        return sb.toString().trim();
    }
}
