// 冰元素抗性
// 减少受到的冰元素伤害

var priority = 42;
var combatPower = 1.0;
var attributeName = "ice_resistance";
var attributeType = "Other";
var placeholder = "ice_resistance";
var pattern = "冰元素抗性";
var patternSuffix = "";

// 注意: 抗性属性不需要实现 runDefense 函数
// 抗性会在 DamageBucket.applyResistances() 中自动应用
// 公式: damage * (1 - resistance / (resistance + 100))
// 例如: 50点冰抗性 -> 减伤 50/(50+100) = 33.3%
