package com.scanoss.settings;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
public class Bom {
    private @Singular("include") List<Rule> include;
    private @Singular("ignore") List<Rule> ignore;
    private @Singular("remove") List<RemoveRule> remove;
    private @Singular("replace") List<ReplaceRule> replace;
}


