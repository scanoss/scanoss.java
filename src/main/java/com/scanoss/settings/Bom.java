package com.scanoss.settings;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
@Builder
public class Bom {
        private List<Rule> include;
        private List<Rule> remove;
        private List<ReplaceRule> replace;


        public void addInclude(@NotNull Rule rule) {
            this.include.add(rule);
        }

        public void addRemove(@NotNull Rule rule) {
            this.include.add(rule);
        }


}


