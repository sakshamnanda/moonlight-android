"""Convert the vendored Apache-2.0 Hacker's Keyboard English layouts to PC events.

Run from the repository root. No network or Android SDK is required.
"""
import json
from pathlib import Path
import xml.etree.ElementTree as ET

source = Path('third_party/hackerskeyboard')
strings = {e.attrib['name']: (e.text or '').replace('\\@', '@').replace('\\"', '"').replace("\\'", "'").replace('\\\\', '\\')
           for e in ET.parse(source / 'donottranslate-keymap.xml').getroot()}
ns = '{http://schemas.android.com/apk/res-auto}'
special = dict(tab=61, return_=66, space=62, shift=59, delete=67,
               ctrl_left=113, alt_left=57, meta_left=117, escape=111,
               fn=-1, settings=115, f1=115, dpad_center=23,
               dpad_up=19, dpad_down=20, dpad_left=21, dpad_right=22,
               page_up=92, page_down=93, home=122, end=123, insert=124,
               forward_del=112, sysrq=120, scroll_lock=116, break_=121, num_lock=143)
special['return'] = special.pop('return_')
special['break'] = special.pop('break_')
special.update({f'fkey_f{i}':130+i for i in range(1,13)})
labels = {61:'⇥',66:'↵',62:'English (US)',59:'⇧',67:'⌫',115:'Caps',
          117:'Win',23:'●',19:'↑',20:'↓',21:'←',22:'→'}
chars = {chr(i+97):29+i for i in range(26)}
chars.update({str(i):7+i for i in range(10)})
chars.update(dict(zip('`,./;\'[]\\-=',[68,55,56,76,74,75,71,72,73,69,70])))
shifted = dict(zip('~!@#$%^&*()_+{}|:"<>?', '`1234567890-=[]\\;\',./'))

for name in ('kbd_full','kbd_full_fn'):
    root = ET.parse(source / (name+'.xml')).getroot()
    rows=[]
    for row in root.findall('Row'):
        if row.get(ns+'extension') == 'true':
            continue  # F1–F12 are available on the Fn page, keeping five tall rows.
        keys=[]
        for key in row.findall('Key'):
            def get(attr):
                value=key.get(ns+attr,'')
                return strings[value[8:]] if value.startswith('@string/') else value
            label=get('keyLabel')
            symbol=get('codes')
            if not label and not symbol:
                continue  # Empty ISO-only key in the upstream US layout.
            shift=False
            if symbol:
                code=special[symbol.removeprefix('@integer/key_')]
            else:
                character=label.lower()
                shift=character in shifted
                code=chars[shifted.get(character,character)]
            label=labels.get(code,label)
            keys.append(dict(label=label, hint=get('shiftLabel') if code!=62 else '',
                             code=code, shift=shift,
                             weight=float(get('keyWidth').replace('%p','') or '7.1428571485')))
        rows.append(keys)
    assert len(rows)==5
    Path('app/src/main/assets/hackerskeyboard/'+name+'.json').write_text(
        json.dumps(rows,ensure_ascii=False,indent=2)+'\n')
