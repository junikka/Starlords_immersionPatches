# 04 Settings And Console

152 review entries. This is an editorial candidate list, including context-dependent terms that may be retained.

Keep placeholders and identifiers unchanged. Fill `Replacement` and record `keep` where the original fits. Java entries are literal fragments: use the JSON context to rewrite the complete expression.

## data/config/LunaSettings.csv

### S-0ddca5e536 — line 4

- Location: `fetures_newLordsInNewGame.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Generate additional lords on new game start
- Replacement: 
- Notes: 

### S-6058587d7d — line 4

- Location: `fetures_newLordsInNewGame.fieldDescription`
- Review: rewrite_candidate; titles_and_collectives
- Original: If set to true, when starting a new game factions will create their own randomly generated lords, provided said faction does not already have any.
- Replacement: 
- Notes: 

### S-c816a534fc — line 5

- Location: `fetures_lifeAndDeathSystem.fieldDescription`
- Review: rewrite_candidate; titles_and_collectives
- Original: If set to true, lords can be permanently killable and new lords will enter the game as time goes on.
- Replacement: 
- Notes: 

### S-fc918f821e — line 7

- Location: `text_AttackSettings.defaultValue`
- Review: rewrite_candidate; titles_and_collectives
- Original: Configure whether hostile lords attack the player faction, and how aggressively.
- Replacement: 
- Notes: 

### S-12d1b7766c — line 8

- Location: `features_canPlayerFactionBeAttacked.fieldDescription`
- Review: rewrite_candidate; titles_and_collectives
- Original: If set to true, the player's faction can be attacked by hostile lords.
- Replacement: 
- Notes: 

### S-7098f01dd3 — line 9

- Location: `features_canPlayerFactionBeAttackedBeforeStarlords.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Allow attacks before any lords join
- Replacement: 
- Notes: 

### S-5f3e5124ba — line 9

- Location: `features_canPlayerFactionBeAttackedBeforeStarlords.fieldDescription`
- Review: rewrite_candidate; titles_and_collectives
- Original: If set to true, the player's faction can be attacked before any lords join the faction. Has no effect if 'Allow attacks on player faction' is false.
- Replacement: 
- Notes: 

### S-a1a65f3b24 — line 11

- Location: `Header_lordRelationSettings.defaultValue`
- Review: rewrite_candidate; titles_and_collectives
- Original: Lord Intel Settings
- Replacement: 
- Notes: 

### S-79b75a535f — line 12

- Location: `Other_requiredRepForDirectContact.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Required reputation for directly opening communications from the lords tab
- Replacement: 
- Notes: 

### S-eabfbc204e — line 13

- Location: `Other_requiredRepForKnowledgeOfLocation.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Required reputation for knowing a lord's location from the lords tab
- Replacement: 
- Notes: 

### S-b55c8dd432 — line 14

- Location: `Other_requiredRepForKnowledgeOfShips.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Required reputation for knowing a lord's ships from the lords tab
- Replacement: 
- Notes: 

### S-070d56ea19 — line 15

- Location: `Other_requiredRepForKnowledgeOfWealth.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Required reputation for knowing a lord's wealth from the lords tab
- Replacement: 
- Notes: 

### S-02efbc35a2 — line 16

- Location: `Other_requiredRepForKnowledgeOfCurAction.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Required reputation for knowing a lord's current action from the lords tab
- Replacement: 
- Notes: 

### S-68abd7a552 — line 17

- Location: `Other_showAllLordsIntelByDefault.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Show lords in the intel tab before you meet them
- Replacement: 
- Notes: 

### S-8dad69d001 — line 18

- Location: `Other_showMessageLordCaptureReleaseEscape.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Show UI Message when lords are captured, released, or escape
- Replacement: 
- Notes: 

### S-e975ec628b — line 18

- Location: `Other_showMessageLordCaptureReleaseEscape.fieldDescription`
- Review: rewrite_candidate; titles_and_collectives
- Original: This setting is always true for lords of your own faction or faction you have a commission with.
- Replacement: 
- Notes: 

### S-90e92a3d7f — line 20

- Location: `Other_minStabilityToPreventFiefDefection.fieldName`
- Review: rewrite_candidate; territory_and_fealty
- Original: Minimum stability to prevent fief defection
- Replacement: 
- Notes: 

### S-e01883e1eb — line 20

- Location: `Other_minStabilityToPreventFiefDefection.fieldDescription`
- Review: rewrite_candidate; titles_and_collectives, territory_and_fealty
- Original: Fief markets at or above this stability level will not defect. If stability drops below this value, the lord may defect and take the market with them.
- Replacement: 
- Notes: 

### S-7c79188fe1 — line 22

- Location: `Header_lordFleetSettings.defaultValue`
- Review: rewrite_candidate; titles_and_collectives
- Original: Lord Fleet Settings
- Replacement: 
- Notes: 

### S-145a56baa8 — line 23

- Location: `Other_maxSMods.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Maximum number of S-Mods a lord can have on each ship
- Replacement: 
- Notes: 

### S-b521e8cf26 — line 26

- Location: `EL_EnableAdditionalLords.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Allow additional lords for all factions
- Replacement: 
- Notes: 

### S-47733c05a7 — line 26

- Location: `EL_EnableAdditionalLords.fieldDescription`
- Review: rewrite_candidate; titles_and_collectives
- Original: Every faction will gain additional lords, regardless of whether they have any already.
- Replacement: 
- Notes: 

### S-c7eab93b94 — line 26

- Location: `EL_EnableAdditionalLords.tab`
- Review: rewrite_candidate; titles_and_collectives
- Original: Extra Lords
- Replacement: 
- Notes: 

### S-97ac314aad — line 27

- Location: `EL_EnableMaxLords.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Enable maximum total lord cap
- Replacement: 
- Notes: 

### S-40ddd85f5a — line 27

- Location: `EL_EnableMaxLords.fieldDescription`
- Review: rewrite_candidate; titles_and_collectives
- Original: Lords will not be generated past the maximum amount. Instead, the game will attempt to reduce every faction's generated lords so a number around the max amount is generated. Only affects randomly generated lords, and not the statically defined ones in json.
- Replacement: 
- Notes: 

### S-5dec6d1d9a — line 27

- Location: `EL_EnableMaxLords.tab`
- Review: rewrite_candidate; titles_and_collectives
- Original: Extra Lords
- Replacement: 
- Notes: 

### S-2428b158b1 — line 28

- Location: `EL_MaxGeneratedLords.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: The maximum number of lords that will be generated
- Replacement: 
- Notes: 

### S-eb72b0e4c1 — line 28

- Location: `EL_MaxGeneratedLords.fieldDescription`
- Review: rewrite_candidate; titles_and_collectives
- Original: This will calculate each tier of lords individually, and then round down. This will only trigger if the calculated number of lords to generate is greater than this value.  This factors in statically defined lords.
- Replacement: 
- Notes: 

### S-faaadbc14b — line 28

- Location: `EL_MaxGeneratedLords.tab`
- Review: rewrite_candidate; titles_and_collectives
- Original: Extra Lords
- Replacement: 
- Notes: 

### S-a20c503ab1 — line 30

- Location: `Header_LordsPerSize.tab`
- Review: rewrite_candidate; titles_and_collectives
- Original: Extra Lords
- Replacement: 
- Notes: 

### S-6f691ac4ab — line 30

- Location: `Header_LordsPerSize.defaultValue`
- Review: rewrite_candidate; titles_and_collectives
- Original: Lords Per Faction Size
- Replacement: 
- Notes: 

### S-73b9afbec7 — line 31

- Location: `text_LordsPerSize.tab`
- Review: rewrite_candidate; titles_and_collectives
- Original: Extra Lords
- Replacement: 
- Notes: 

### S-b0116e8bcd — line 31

- Location: `text_LordsPerSize.defaultValue`
- Review: rewrite_candidate; titles_and_collectives
- Original: This handles the number of lords generated based on total combined market size of a given faction. 
- Replacement: 
- Notes: 

### S-9e5e3d74cf — line 32

- Location: `EL_T0PerSize.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Tier 0 lords per total combined size
- Replacement: 
- Notes: 

### S-b2e854bac8 — line 32

- Location: `EL_T0PerSize.tab`
- Review: rewrite_candidate; titles_and_collectives
- Original: Extra Lords
- Replacement: 
- Notes: 

### S-f0ae327a00 — line 33

- Location: `EL_T1PerSize.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Tier 1 lords per total combined size
- Replacement: 
- Notes: 

### S-f17a1d4d4a — line 33

- Location: `EL_T1PerSize.tab`
- Review: rewrite_candidate; titles_and_collectives
- Original: Extra Lords
- Replacement: 
- Notes: 

### S-8be19e8e44 — line 34

- Location: `EL_T2PerSize.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Tier 2 lords per total combined size
- Replacement: 
- Notes: 

### S-76ab914d04 — line 34

- Location: `EL_T2PerSize.tab`
- Review: rewrite_candidate; titles_and_collectives
- Original: Extra Lords
- Replacement: 
- Notes: 

### S-b042995a29 — line 36

- Location: `Header_LordsAdditional.tab`
- Review: rewrite_candidate; titles_and_collectives
- Original: Extra Lords
- Replacement: 
- Notes: 

### S-e28ceb743c — line 36

- Location: `Header_LordsAdditional.defaultValue`
- Review: rewrite_candidate; titles_and_collectives
- Original: Additional Lords
- Replacement: 
- Notes: 

### S-b8eed9ad9e — line 37

- Location: `text_LordsAdditional.tab`
- Review: rewrite_candidate; titles_and_collectives
- Original: Extra Lords
- Replacement: 
- Notes: 

### S-bb4f48ec0a — line 37

- Location: `text_LordsAdditional.defaultValue`
- Review: rewrite_candidate; titles_and_collectives
- Original: This handles the number of additional lords generated in each faction as a static amount on top of what is already calculated.
- Replacement: 
- Notes: 

### S-0a8ac910e7 — line 38

- Location: `EL_T0Additional.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Additional tier 0 lords
- Replacement: 
- Notes: 

### S-4a3638f074 — line 38

- Location: `EL_T0Additional.tab`
- Review: rewrite_candidate; titles_and_collectives
- Original: Extra Lords
- Replacement: 
- Notes: 

### S-83aba28dfa — line 39

- Location: `EL_T1Additional.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Additional tier 1 lords
- Replacement: 
- Notes: 

### S-ce2dc5120a — line 39

- Location: `EL_T1Additional.tab`
- Review: rewrite_candidate; titles_and_collectives
- Original: Extra Lords
- Replacement: 
- Notes: 

### S-3b5556e086 — line 40

- Location: `EL_T2Additional.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Additional tier 2 lords
- Replacement: 
- Notes: 

### S-e60964778c — line 40

- Location: `EL_T2Additional.tab`
- Review: rewrite_candidate; titles_and_collectives
- Original: Extra Lords
- Replacement: 
- Notes: 

### S-8c52205ceb — line 42

- Location: `Header_LordsOddsOfFief.tab`
- Review: rewrite_candidate; titles_and_collectives
- Original: Extra Lords
- Replacement: 
- Notes: 

### S-e953a4cb37 — line 42

- Location: `Header_LordsOddsOfFief.defaultValue`
- Review: rewrite_candidate; territory_and_fealty
- Original: Odds Of Fief
- Replacement: 
- Notes: 

### S-2ebc612133 — line 43

- Location: `text_LordsOddsOfFief.tab`
- Review: rewrite_candidate; titles_and_collectives
- Original: Extra Lords
- Replacement: 
- Notes: 

### S-1c46defa5f — line 43

- Location: `text_LordsOddsOfFief.defaultValue`
- Review: rewrite_candidate; titles_and_collectives, territory_and_fealty
- Original: Odds of any given lord of a given tier having a fief at new game start. If many lords are selected for a fief, some may be unable to gain a fief regardless of how high these settings are.
- Replacement: 
- Notes: 

### S-13b33cca2a — line 44

- Location: `EL_T0OddsOfFief.fieldName`
- Review: rewrite_candidate; titles_and_collectives, territory_and_fealty
- Original: Odds of fief for tier 0 lords
- Replacement: 
- Notes: 

### S-36ccaddff2 — line 44

- Location: `EL_T0OddsOfFief.tab`
- Review: rewrite_candidate; titles_and_collectives
- Original: Extra Lords
- Replacement: 
- Notes: 

### S-b1efd3e8d4 — line 45

- Location: `EL_T1OddsOfFief.fieldName`
- Review: rewrite_candidate; titles_and_collectives, territory_and_fealty
- Original: Odds of fief for tier 1 lords
- Replacement: 
- Notes: 

### S-49f0276ab2 — line 45

- Location: `EL_T1OddsOfFief.tab`
- Review: rewrite_candidate; titles_and_collectives
- Original: Extra Lords
- Replacement: 
- Notes: 

### S-126e59433c — line 46

- Location: `EL_T2OddsOfFief.fieldName`
- Review: rewrite_candidate; titles_and_collectives, territory_and_fealty
- Original: Odds of fief for tier 2 lords
- Replacement: 
- Notes: 

### S-41971616f8 — line 46

- Location: `EL_T2OddsOfFief.tab`
- Review: rewrite_candidate; titles_and_collectives
- Original: Extra Lords
- Replacement: 
- Notes: 

### S-6b97deaeeb — line 50

- Location: `LAD_AllowExstremests.fieldDescription`
- Review: rewrite_candidate; titles_and_collectives
- Original: If enabled, 'extremists' (such as pirates, or Luddic Path) will spawn instead of a faction lord if market stability is low enough.
- Replacement: 
- Notes: 

### S-eba06fe29b — line 51

- Location: `LAD_AllowLife.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Allow new lords to spawn
- Replacement: 
- Notes: 

### S-1ba344da91 — line 52

- Location: `LAD_AllowDeath.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Allow lords to die
- Replacement: 
- Notes: 

### S-0595b3ded3 — line 55

- Location: `Text_LAD_HardLimits.defaultValue`
- Review: rewrite_candidate; titles_and_collectives
- Original: Absolute limits for this system, lords cannot be generated or killed when past these values.
- Replacement: 
- Notes: 

### S-072d0847c8 — line 56

- Location: `LAD_hardMax.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Hard maximum number of lords
- Replacement: 
- Notes: 

### S-1928e54f37 — line 57

- Location: `LAD_hardMin.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Hard minimum number of lords
- Replacement: 
- Notes: 

### S-548d544ab0 — line 60

- Location: `Text_LAD_SoftLimits.defaultValue`
- Review: rewrite_candidate; titles_and_collectives
- Original: Control the spawn/death rates once lord counts moves outside the soft range, pushing it back into the soft range. 
- Replacement: 
- Notes: 

### S-4f6e6b48c9 — line 61

- Location: `LAD_softMax.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Soft maximum number of lords
- Replacement: 
- Notes: 

### S-0384e0f69b — line 62

- Location: `LAD_softMin.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Soft minimum number of lords
- Replacement: 
- Notes: 

### S-d70c132a4f — line 63

- Location: `LAD_softSpeedup.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Speedup per lord under min
- Replacement: 
- Notes: 

### S-964a9dcb9b — line 63

- Location: `LAD_softSpeedup.fieldDescription`
- Review: rewrite_candidate; titles_and_collectives
- Original: Calculates speedup as a multiplier equal to 1 + (Speedup per lord under min * missing lords) to increase lord spawn rate.
- Replacement: 
- Notes: 

### S-b4c2a54120 — line 64

- Location: `LAD_softSlowdown.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Slowdown per lord over max
- Replacement: 
- Notes: 

### S-cd600b02a2 — line 64

- Location: `LAD_softSlowdown.fieldDescription`
- Review: rewrite_candidate; titles_and_collectives
- Original: Calculates slowdown as a multiplier equal to (Slowdown per lord over max ^ extra lords) to increase lord death chance.
- Replacement: 
- Notes: 

### S-b7918de9f3 — line 66

- Location: `Header_LAD_PointGains.defaultValue`
- Review: rewrite_candidate; titles_and_collectives
- Original: Lord Spawn Requirements And Gains
- Replacement: 
- Notes: 

### S-9737f7b6ca — line 67

- Location: `Text_LAD_PointGains.defaultValue`
- Review: rewrite_candidate; titles_and_collectives
- Original: This controls the number of 'points' a market needs to accumulate to spawn a lord, as well as how quickly each market can generate points.<br>Each market holds its own store of 'points' it uses to generate lords. Points are gained every month.<br>The equation used is ((size * size mult) + (size exp ^ size)) * 1 - (missing stability * stability penalty).<br>
- Replacement: 
- Notes: 

### S-f7db342d31 — line 71

- Location: `LAD_PonitsRequired.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Points required to generate lord
- Replacement: 
- Notes: 

### S-821c37c661 — line 81

- Location: `Text_LAD_ponitsRequired.defaultValue`
- Review: rewrite_candidate; titles_and_collectives
- Original: The chance a lord will die if defeated in any given battle. 
- Replacement: 
- Notes: 

### S-d5fe1c7b0a — line 82

- Location: `LAD_BaseOddsOfDeath.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Odds of lord death
- Replacement: 
- Notes: 

### S-2960421b8e — line 85

- Location: `text_LAD_StartingPoints.defaultValue`
- Review: rewrite_candidate; titles_and_collectives
- Original: Ratio of a market having 'starting points' on new game, allowing variance of when lords start to spawn.
- Replacement: 
- Notes: 

### S-317a4e2dc2 — line 93

- Location: `Header_LordLevel.defaultValue`
- Review: rewrite_candidate; titles_and_collectives
- Original: Lord Level Ratio
- Replacement: 
- Notes: 

### S-b08b6c3183 — line 94

- Location: `text_lordLevel.defaultValue`
- Review: rewrite_candidate; titles_and_collectives
- Original: Ratio of possible lord levels.
- Replacement: 
- Notes: 

### S-7bac21b453 — line 95

- Location: `generator_lordLevel_min.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Min lord level
- Replacement: 
- Notes: 

### S-0dc325cbc9 — line 96

- Location: `generator_lordLevel_max.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Max lord level
- Replacement: 
- Notes: 

### S-8ff5c3696c — line 97

- Location: `generator_lordLevel_target.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Target lord level
- Replacement: 
- Notes: 

### S-f2dda0eea1 — line 103

- Location: `text_frigateRatio.defaultValue`
- Review: rewrite_candidate; titles_and_collectives
- Original: The ratio of frigates to other ships in a generated lord's fleet.
- Replacement: 
- Notes: 

### S-9ee6772efb — line 110

- Location: `text_destroyerRatio.defaultValue`
- Review: rewrite_candidate; titles_and_collectives
- Original: The ratio of destroyers to other ships in a generated lord's fleet.
- Replacement: 
- Notes: 

### S-cbceda7807 — line 117

- Location: `text_cruiserRatio.defaultValue`
- Review: rewrite_candidate; titles_and_collectives
- Original: The ratio of cruisers to other ships in a generated lord's fleet.
- Replacement: 
- Notes: 

### S-c189b3f899 — line 124

- Location: `text_captialRatio.defaultValue`
- Review: rewrite_candidate; titles_and_collectives
- Original: The ratio of capitals to other ships in a generated lord's fleet.
- Replacement: 
- Notes: 

### S-3aa4c7b02c — line 133

- Location: `text_warshipRatio.defaultValue`
- Review: rewrite_candidate; titles_and_collectives
- Original: The ratio of warships to other ship types in a generated lord's fleet.
- Replacement: 
- Notes: 

### S-d90e8599a8 — line 140

- Location: `text_carrierRatio.defaultValue`
- Review: rewrite_candidate; titles_and_collectives
- Original: The ratio of carriers to other ship types in a generated lord's fleet.
- Replacement: 
- Notes: 

### S-66e8eb95e6 — line 147

- Location: `text_phaseRatio.defaultValue`
- Review: rewrite_candidate; titles_and_collectives
- Original: The ratio of phase ships to other ship types in a generated lord's fleet.
- Replacement: 
- Notes: 

### S-581a3d0782 — line 154

- Location: `Header_personalityRatio.defaultValue`
- Review: rewrite_candidate; titles_and_collectives
- Original: Lord Personality Odds
- Replacement: 
- Notes: 

### S-cd8ec1bf45 — line 155

- Location: `generator_personalityRatio_Quarrelsome.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Odds of a given lord having a Quarrelsome personality
- Replacement: 
- Notes: 

### S-d6cc299238 — line 156

- Location: `generator_personalityRatio_Calculating.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Odds of a given lord having a Calculating personality
- Replacement: 
- Notes: 

### S-0a463910d8 — line 157

- Location: `generator_personalityRatio_Martial.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Odds of a given lord having a Martial personality
- Replacement: 
- Notes: 

### S-566cf11c94 — line 158

- Location: `generator_personalityRatio_Upstanding.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Odds of a given lord having a Upstanding personality
- Replacement: 
- Notes: 

### S-a0f4a42cce — line 159

- Location: `generator_battlePersonalityRatio_Timid.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Odds of a given lord having a Timid battle personality
- Replacement: 
- Notes: 

### S-d7f85ff236 — line 160

- Location: `generator_battlePersonalityRatio_Cautious.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Odds of a given lord having a Cautious battle personality
- Replacement: 
- Notes: 

### S-5ea9cea66b — line 161

- Location: `generator_battlePersonalityRatio_Steady.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Odds of a given lord having a Steady battle personality
- Replacement: 
- Notes: 

### S-85efd89c6c — line 162

- Location: `generator_battlePersonalityRatio_Aggressive.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Odds of a given lord having a Aggressive battle personality
- Replacement: 
- Notes: 

### S-39733d5b62 — line 163

- Location: `generator_battlePersonalityRatio_Reckless.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Odds of a given lord having a Reckless battle personality
- Replacement: 
- Notes: 

### S-6d2241ca38 — line 166

- Location: `text_FleetTemplates.defaultValue`
- Review: rewrite_candidate; titles_and_collectives
- Original: Odds of a given fleet generator being used on a lord's randomly generated fleet. Set to 0 to disable the generator. 
- Replacement: 
- Notes: 

### S-08d28c55b9 — line 167

- Location: `generator_fleetTemplates_system.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Odds of a given lord choosing their ships based of ship system
- Replacement: 
- Notes: 

### S-454b6857d7 — line 168

- Location: `generator_fleetTemplates_hullmod.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Odds of a given lord choosing their ships based on hullmods
- Replacement: 
- Notes: 

### S-4bc0a487bb — line 169

- Location: `generator_fleetTemplates_manufacture.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Odds of a given lord choosing their ships based on manufacturer
- Replacement: 
- Notes: 

### S-21b63e94fc — line 170

- Location: `generator_fleetTemplates_random.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Odds of a given lord choosing their ships completely randomly
- Replacement: 
- Notes: 

### S-50b735d7e1 — line 173

- Location: `text_FlagshipTemplates.defaultValue`
- Review: rewrite_candidate; titles_and_collectives
- Original: Odds of a given flagship generator being used on a lord's randomly generated flagship. Set to 0 to disable the generator. 
- Replacement: 
- Notes: 

### S-d58ba6752d — line 174

- Location: `generator_flagshipPicker_Random.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Odds of a given lord choosing their flagship randomly
- Replacement: 
- Notes: 

### S-f19a51ed16 — line 175

- Location: `generator_flagshipPicker_HP.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Odds of a given lord choosing their flagship based on its HP
- Replacement: 
- Notes: 

### S-e28bd7deb6 — line 176

- Location: `generator_flagshipPicker_Cost.fieldName`
- Review: rewrite_candidate; titles_and_collectives
- Original: Odds of a given lord choosing their flagship based on its cost
- Replacement: 
- Notes: 

### S-1ed3249759 — line 179

- Location: `generator_isMaleChance.fieldDescription`
- Review: rewrite_candidate; titles_and_collectives
- Original: The odds of a given lord being male. set to 0 for every lord to be female, set to 1 for every lord to be male
- Replacement: 
- Notes: 

### S-c500139a5e — line 180

- Location: `generator_fleetAdjective.fieldDescription`
- Review: rewrite_candidate; titles_and_collectives
- Original: The substring that goes on the end of a given lords fleet name. All lord fleets start with their name and end with this value.<br>Example lord: Anderson Jackson, Example suffix: "'s fleet", Full fleet name: "Anderson Jackson's fleet"
- Replacement: 
- Notes: 

### S-2b17d0af4d — line 182

- Location: `generator_oddsOfNonePriorityShips.fieldDescription`
- Review: rewrite_candidate; titles_and_collectives
- Original: The odds of any given lord using ships that are not prioritized by there faction
- Replacement: 
- Notes: 

### S-410b50c5b3 — line 183

- Location: `generator_oddsOfNoneSelectedFlagship.fieldDescription`
- Review: rewrite_candidate; titles_and_collectives
- Original: The odds of any given lord using a flagship that is not present in there fleet in any other way
- Replacement: 
- Notes: 

### S-149a3e1130 — line 192

- Location: `text_maxShipSpawnRatio.defaultValue`
- Review: rewrite_candidate; titles_and_collectives
- Original: Applies a random multiplier to determine the maximum number of ships a lord's fleet can have.
- Replacement: 
- Notes: 

### S-6ae6c0bd18 — line 199

- Location: `text_minShipSpawnRatio.defaultValue`
- Review: rewrite_candidate; titles_and_collectives
- Original: Applies a random multiplier to determine the minimum number of ships a lord's fleet can have.
- Replacement: 
- Notes: 

## data/console/commands.csv

### S-739bbd8c31 — line 2

- Location: `WarpLord.help`
- Review: rewrite_candidate; titles_and_collectives
- Original:  Teleports the specified lord next to you.
- Replacement: 
- Notes: 

### S-4d30529913 — line 3

- Location: `RespawnLord.help`
- Review: rewrite_candidate; titles_and_collectives
- Original:  Destroys lord fleet and begins respawn.
- Replacement: 
- Notes: 

### S-597cad371b — line 4

- Location: `AddLordWealth.help`
- Review: rewrite_candidate; titles_and_collectives
- Original:  Add or remove the specified wealth from the lord.
- Replacement: 
- Notes: 

### S-95bb6bb92f — line 5

- Location: `AddLordRaid.help`
- Review: rewrite_candidate; titles_and_collectives
- Original:  Add raid for lord on target market.
- Replacement: 
- Notes: 

### S-4d9ba48330 — line 6

- Location: `AddLordRelation.help`
- Review: rewrite_candidate; titles_and_collectives
- Original:  Increase or decrease player relations with the lord.
- Replacement: 
- Notes: 

### S-7388705a56 — line 7

- Location: `AddLordLoyalty.help`
- Review: rewrite_candidate; titles_and_collectives
- Original:  Increase or decrease lord loyalty.
- Replacement: 
- Notes: 

### S-def6da439e — line 8

- Location: `AddLordFief.help`
- Review: rewrite_candidate; titles_and_collectives
- Original:  Grants the specified market to the lord. Must be of the same faction.
- Replacement: 
- Notes: 

### S-e2d1c042b2 — line 9

- Location: `RemoveLordFief.help`
- Review: rewrite_candidate; titles_and_collectives, territory_and_fealty
- Original:  Removes the specified market from the lord's fiefs if present.
- Replacement: 
- Notes: 

### S-26568c92d5 — line 10

- Location: `AddLordRequest.help`
- Review: rewrite_candidate; titles_and_collectives
- Original:  Creates a request for the lord with the defined type (prison_break
- Replacement: 
- Notes: 

### S-e1af008732 — line 13

- Location: `PrintLordsRequests.help`
- Review: rewrite_candidate; titles_and_collectives
- Original:  Print existing Lord Requests to log
- Replacement: 
- Notes: 

### S-709b55f64f — line 14

- Location: `PrintLordsDetails.help`
- Review: rewrite_candidate; titles_and_collectives
- Original:  Print tab separated lord faction
- Replacement: 
- Notes: 

### S-7d4efcfb74 — line 15

- Location: `PrintLordTravel.help`
- Review: rewrite_candidate; titles_and_collectives
- Original:  Print lord's distance
- Replacement: 
- Notes: 

### S-c4454e8f86 — line 16

- Location: `PrintLordsAlignments.help`
- Review: rewrite_candidate; titles_and_collectives
- Original:  "Print tab separated lord id
- Replacement: 
- Notes: 

### S-449d44fdef — line 17

- Location: `PrintLordsDefectionWeights.help`
- Review: rewrite_candidate; titles_and_collectives
- Original:  "Print tab separated lord id
- Replacement: 
- Notes: 

## mod_info.json

### S-c97dce7692 — line 3

- Location: `/name`
- Review: rewrite_candidate; titles_and_collectives
- Original: Star Lords
- Replacement: 
- Notes: 

### S-8f5c7cbe9a — line 10

- Location: `/description`
- Review: rewrite_candidate; meta_or_tone
- Original: Mount and Blade in Space
- Replacement: 
- Notes: 

## src/starlords/console/AddLordFief.java

### S-2dadf67d8e — line 27

- Location: `java:27:1`
- Review: rewrite_candidate; titles_and_collectives
- Original: Lord not found.
- Replacement: 
- Notes: 

### S-8ad6a85e7c — line 43

- Location: `java:43:1`
- Review: rewrite_candidate; titles_and_collectives
- Original: Cannot grant market to a lord of a different faction.
- Replacement: 
- Notes: 

## src/starlords/console/AddLordLoyalty.java

### S-e3dc703757 — line 23

- Location: `java:23:1`
- Review: rewrite_candidate; titles_and_collectives
- Original: Lord not found.
- Replacement: 
- Notes: 

## src/starlords/console/AddLordRaid.java

### S-ac6e3b5e7c — line 29

- Location: `java:29:1`
- Review: rewrite_candidate; titles_and_collectives
- Original: Lord not found.
- Replacement: 
- Notes: 

### S-7484d2c827 — line 38

- Location: `java:38:2`
- Review: rewrite_candidate; titles_and_collectives
- Original:  with originator lord 
- Replacement: 
- Notes: 

## src/starlords/console/AddLordRelation.java

### S-d3100a6948 — line 22

- Location: `java:22:1`
- Review: rewrite_candidate; titles_and_collectives
- Original: Lord not found.
- Replacement: 
- Notes: 

## src/starlords/console/AddLordRequest.java

### S-d80116f2b4 — line 21

- Location: `java:21:1`
- Review: rewrite_candidate; titles_and_collectives
- Original: Lord not found.
- Replacement: 
- Notes: 

## src/starlords/console/AddLordWealth.java

### S-f266865858 — line 22

- Location: `java:22:1`
- Review: rewrite_candidate; titles_and_collectives
- Original: Lord not found.
- Replacement: 
- Notes: 

## src/starlords/console/PrintLordsAlignments.java

### S-e2b05a9fcb — line 14

- Location: `java:14:1`
- Review: rewrite_candidate; titles_and_collectives
- Original: Print full lord list alignments to log
- Replacement: 
- Notes: 

## src/starlords/console/PrintLordsDefectionWeights.java

### S-3ad0f13db5 — line 18

- Location: `java:18:1`
- Review: rewrite_candidate; titles_and_collectives
- Original: Print full lord list defection weights to log
- Replacement: 
- Notes: 

## src/starlords/console/PrintLordsDetails.java

### S-1accae3bf1 — line 14

- Location: `java:14:1`
- Review: rewrite_candidate; titles_and_collectives
- Original: Print full lord list details to log
- Replacement: 
- Notes: 

## src/starlords/console/PrintLordsRequests.java

### S-da7a4a0a46 — line 14

- Location: `java:14:1`
- Review: rewrite_candidate; titles_and_collectives
- Original: Print existing lord requests to log
- Replacement: 
- Notes: 

## src/starlords/console/PrintLordTravel.java

### S-f67d2d88e3 — line 22

- Location: `java:22:1`
- Review: rewrite_candidate; titles_and_collectives
- Original: No Lord provided. Using player
- Replacement: 
- Notes: 

### S-646ef7a474 — line 34

- Location: `java:34:1`
- Review: rewrite_candidate; titles_and_collectives
- Original: Lord has no current target.
- Replacement: 
- Notes: 

### S-e5cf4f9ffa — line 41

- Location: `java:41:1`
- Review: rewrite_candidate; titles_and_collectives
- Original: Lord 
- Replacement: 
- Notes: 

## src/starlords/console/RemoveLordFief.java

### S-bb8295904a — line 25

- Location: `java:25:1`
- Review: rewrite_candidate; titles_and_collectives
- Original: Lord not found.
- Replacement: 
- Notes: 

## src/starlords/console/RespawnLord.java

### S-d86cff7601 — line 24

- Location: `java:24:1`
- Review: rewrite_candidate; titles_and_collectives
- Original: Lord not found.
- Replacement: 
- Notes: 

### S-3f7149e306 — line 35

- Location: `java:35:1`
- Review: rewrite_candidate; titles_and_collectives
- Original: Respawning lord 
- Replacement: 
- Notes: 

## src/starlords/console/WarpLord.java

### S-4114963a12 — line 31

- Location: `java:31:1`
- Review: rewrite_candidate; titles_and_collectives
- Original: Lord not found.
- Replacement: 
- Notes: 

### S-dbe1287a1b — line 35

- Location: `java:35:1`
- Review: rewrite_candidate; titles_and_collectives
- Original: Lord currently has no fleet.
- Replacement: 
- Notes: 

### S-9876137fd3 — line 51

- Location: `java:51:1`
- Review: rewrite_candidate; titles_and_collectives
- Original: Warped lord 
- Replacement: 
- Notes: 
