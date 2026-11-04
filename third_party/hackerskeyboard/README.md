# Hacker's Keyboard layout integration

Source: https://github.com/klausw/hackerskeyboard
Commit: 9202d9d8b1d379f1a20edf08ed7e7149038c8e43
License: Apache-2.0 (full text in LICENSE). Copyright notices are retained in
the upstream XML files. These files originate from the Android Open Source
Project and Hacker's Keyboard contributors.

The bundled English US normal and Fn layouts are generated from kbd_full.xml,
kbd_full_fn.xml and donottranslate-keymap.xml with tools/import_hacker_keyboard.py.
Generated assets are used directly by GamingKeyboard. Android PC keycodes
replace upstream IME codes. Blank ISO keys and the optional extension row are
omitted; Fn provides F1–F12, navigation and number keys. The IME settings key
becomes Caps Lock. Samsung-inspired rendering, haptics, modifier state and
Moonlight event forwarding are new code. This does not install a system IME
or include Samsung code, dictionaries, predictions or language packs.

Rebuild assets from the repository root:

    python3 tools/import_hacker_keyboard.py
