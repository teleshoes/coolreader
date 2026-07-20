git checkout master
git branch -D newmaster
git checkout buggins/master -b newmaster
for x in \
  deploy-android-build-tweaks \
  cr_state_log_file \
  audiobook_in_tts \
; # audiobook_jump_forward_back
do
  git merge --no-ff -m "Merge branch '$x'" $x
done
git checkout master
