echo BUILDING \
  && rm -rf build/ \
  && mkdir build/ \
  && cd build/ \
  && cmake -DCMAKE_INSTALL_PREFIX:PATH=/usr .. \
  && make clean \
  && make -j8 \
  && sudo make install \
  && cd .. \
  && rm -f cr3qt/src/i18n/cr3_*.ts \
  && git checkout cr3qt/src/i18n/cr3_*.ts \
  && echo DONE
