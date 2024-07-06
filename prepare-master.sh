git checkout master
git branch -D newmaster
git checkout buggins/master -b newmaster
for x in \
  deploy-android-build-tweaks \
  selection-text-tweaks \
  intents_for_reader_actions \
  tts_hide_toolbar \
  tts_toolbar_prevent_re_init \
  tts_fix_prev_next_while_paused \
  tts_fix_stoptts_crashing \
  tts_add_tts_stop_readeraction_intent \
  page_header_in_scroll_mode_tts \
  cr_state_log_file \
  audiobook_in_tts \
; # audiobook_jump_forward_back
do
  git merge --no-ff -m "Merge branch '$x'" $x
done
git checkout master
