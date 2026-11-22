//! Held Item Info for Horizon: displays enchantments, potion contents, unbreakable status,
//! and container summaries above the hotbar for the currently held item.

use mod_api::{self, ClientHost, Compound, Hooks, RenderGuiPostEvent, Tag};

const INFO_COLOR: u32 = 0xFFCC_CCCC;
const MAX_LINES: usize = 6;

fn compound_get<'a>(map: &'a Compound, key: &str) -> Option<&'a Tag> {
    map.iter()
        .find(|(k, _)| k.to_string().ok().as_deref() == Some(key))
        .map(|(_, v)| v)
}

fn tag_as_i32(tag: &Tag) -> Option<i32> {
    match tag {
        Tag::Int(v) => Some(*v),
        Tag::Short(v) => Some(*v as i32),
        Tag::Byte(v) => Some(*v as i32),
        _ => None,
    }
}

fn to_roman(n: i32) -> String {
    match n {
        1 => "I".to_string(),
        2 => "II".to_string(),
        3 => "III".to_string(),
        4 => "IV".to_string(),
        5 => "V".to_string(),
        6 => "VI".to_string(),
        7 => "VII".to_string(),
        8 => "VIII".to_string(),
        9 => "IX".to_string(),
        10 => "X".to_string(),
        other => other.to_string(),
    }
}

fn format_id_name(id: &str) -> String {
    let raw = id.split_once(':').map(|(_, p)| p).unwrap_or(id);
    raw.split('_')
        .filter(|s| !s.is_empty())
        .map(|w| {
            let mut chars = w.chars();
            match chars.next() {
                Some(first) => format!("{}{}", first.to_uppercase(), chars.as_str()),
                None => String::new(),
            }
        })
        .collect::<Vec<_>>()
        .join(" ")
}

fn collect_enchantment_lines(tag: &Tag, out: &mut Vec<String>) {
    let Tag::Compound(map) = tag else {
        return;
    };
    if let Some(Tag::Byte(0)) = compound_get(map, "show_in_tooltip") {
        return;
    }
    let levels: &Compound = match compound_get(map, "levels") {
        Some(Tag::Compound(inner)) => inner,
        _ => map,
    };
    for (key, val) in levels {
        let Ok(k) = key.to_string() else {
            continue;
        };
        if k == "show_in_tooltip" {
            continue;
        }
        if let Some(lvl) = tag_as_i32(val) {
            out.push(format!("{} {}", format_id_name(&k), to_roman(lvl)));
        }
    }
}

fn collect_item_info_lines(nbt: &Compound) -> Vec<String> {
    let mut lines = Vec::new();
    for key in ["minecraft:enchantments", "minecraft:stored_enchantments"] {
        if let Some(tag) = compound_get(nbt, key) {
            collect_enchantment_lines(tag, &mut lines);
        }
    }
    if let Some(Tag::Compound(potion)) = compound_get(nbt, "minecraft:potion_contents") {
        if let Some(Tag::String(id)) = compound_get(potion, "potion") {
            if let Ok(s) = id.to_string() {
                lines.push(format!("Potion: {}", format_id_name(&s)));
            }
        }
    }
    if compound_get(nbt, "minecraft:unbreakable").is_some() {
        lines.push("Unbreakable".to_string());
    }
    if let Some(Tag::List(items)) = compound_get(nbt, "minecraft:container") {
        if !items.is_empty() {
            lines.push(format!("Container: {} slot(s)", items.len()));
        }
    }
    lines.truncate(MAX_LINES);
    lines
}

#[derive(Default)]
struct HeldItemInfoMod;

#[mod_api::horizon_mod]
impl Hooks for HeldItemInfoMod {
    fn render_gui_post(&self, host: &dyn ClientHost, event: &mut RenderGuiPostEvent<'_>) {
        let Some(player) = host.local_player() else {
            return;
        };
        let Some(slots) = player.inventory_slots(host) else {
            return;
        };
        let selected = player.selected_slot(host).unwrap_or(0) as u32;
        let Some((_, stack)) = slots.iter().find(|(idx, _)| *idx == selected) else {
            return;
        };
        let Some(components) = stack.components() else {
            return;
        };
        let lines = collect_item_info_lines(components.as_nbt());
        if lines.is_empty() {
            return;
        }

        let [screen_w, screen_h] = event.draw.size();
        // Render centered above the held item name line (hotbar top is around screen_h - 59).
        let line_h = 10.0_f32;
        let mut y = (screen_h - 68.0 - (lines.len() as f32) * line_h).max(4.0);
        for line in lines {
            let w = event.draw.text_width(&line);
            let x = ((screen_w - w) * 0.5).max(4.0);
            event.draw.text(&line, [x, y], INFO_COLOR, true);
            y += line_h;
        }
    }
}

#[cfg(test)]
mod tests {
    use super::{format_id_name, to_roman};

    #[test]
    fn formats_enchantment_names_and_levels() {
        assert_eq!(format_id_name("minecraft:fire_aspect"), "Fire Aspect");
        assert_eq!(to_roman(2), "II");
        assert_eq!(to_roman(5), "V");
    }
}
