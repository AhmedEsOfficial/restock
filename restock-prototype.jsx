import React, { useState, useEffect } from 'react';

const RestockApp = () => {
  const [activeTab, setActiveTab] = useState('list');
  const [items, setItems] = useState([
    { id: 1, name: 'Milk', checked: false, cycle: 7, lastBought: 6, meal: 'breakfast', suggested: true },
    { id: 2, name: 'Eggs', checked: false, cycle: 10, lastBought: 9, meal: 'breakfast', suggested: true },
    { id: 3, name: 'Bread', checked: false, cycle: 5, lastBought: 2, meal: null, suggested: false },
    { id: 4, name: 'Chicken breast', checked: true, cycle: 7, lastBought: 0, meal: 'dinner', suggested: false },
    { id: 5, name: 'Spinach', checked: false, cycle: 4, lastBought: 3, meal: 'lunch', suggested: true },
  ]);
  
  const [suggestions, setSuggestions] = useState([
    { id: 101, name: 'Greek yogurt', cycle: 7, lastBought: 8, meal: 'breakfast', reason: 'Usually every 7 days' },
    { id: 102, name: 'Bananas', cycle: 5, lastBought: 6, meal: 'snack', reason: 'Running low?' },
    { id: 103, name: 'Olive oil', cycle: 30, lastBought: 32, meal: null, reason: 'Monthly restock' },
  ]);

  const [inputValue, setInputValue] = useState('');
  const [showMealTag, setShowMealTag] = useState(null);
  const [animatingItems, setAnimatingItems] = useState(new Set());

  const meals = ['breakfast', 'lunch', 'dinner', 'snack'];
  const mealEmojis = { breakfast: '🌅', lunch: '☀️', dinner: '🌙', snack: '✨' };
  const mealColors = { 
    breakfast: '#F59E0B', 
    lunch: '#10B981', 
    dinner: '#6366F1', 
    snack: '#EC4899' 
  };

  const toggleItem = (id) => {
    setAnimatingItems(prev => new Set([...prev, id]));
    setTimeout(() => {
      setItems(items.map(item => 
        item.id === id ? { ...item, checked: !item.checked } : item
      ));
      setAnimatingItems(prev => {
        const newSet = new Set(prev);
        newSet.delete(id);
        return newSet;
      });
    }, 200);
  };

  const addSuggestion = (suggestion) => {
    setItems([...items, { ...suggestion, checked: false, suggested: false }]);
    setSuggestions(suggestions.filter(s => s.id !== suggestion.id));
  };

  const dismissSuggestion = (id) => {
    setSuggestions(suggestions.filter(s => s.id !== id));
  };

  const addItem = () => {
    if (inputValue.trim()) {
      const newItem = {
        id: Date.now(),
        name: inputValue.trim(),
        checked: false,
        cycle: null,
        lastBought: 0,
        meal: null,
        suggested: false,
        isNew: true
      };
      setItems([newItem, ...items]);
      setInputValue('');
    }
  };

  const setMealTag = (itemId, meal) => {
    setItems(items.map(item =>
      item.id === itemId ? { ...item, meal: item.meal === meal ? null : meal } : item
    ));
    setShowMealTag(null);
  };

  const uncheckedItems = items.filter(i => !i.checked);
  const checkedItems = items.filter(i => i.checked);

  return (
    <div style={{
      minHeight: '100vh',
      background: 'linear-gradient(180deg, #FFFBF5 0%, #FFF8ED 100%)',
      fontFamily: "'DM Sans', -apple-system, BlinkMacSystemFont, sans-serif",
      position: 'relative',
      overflow: 'hidden'
    }}>
      {/* Subtle texture overlay */}
      <div style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundImage: `url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='1' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)'/%3E%3C/svg%3E")`,
        opacity: 0.03,
        pointerEvents: 'none'
      }} />

      <div style={{
        maxWidth: '420px',
        margin: '0 auto',
        padding: '0 20px',
        paddingBottom: '100px'
      }}>
        {/* Header */}
        <header style={{
          paddingTop: '48px',
          paddingBottom: '32px',
          position: 'relative'
        }}>
          <div style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between'
          }}>
            <div>
              <h1 style={{
                fontSize: '32px',
                fontWeight: '600',
                color: '#1F1F1F',
                margin: 0,
                letterSpacing: '-0.5px'
              }}>
                Restock
              </h1>
              <p style={{
                fontSize: '14px',
                color: '#8B8680',
                margin: '4px 0 0 0',
                fontWeight: '400'
              }}>
                {uncheckedItems.length} items needed
              </p>
            </div>
            <div style={{
              width: '44px',
              height: '44px',
              borderRadius: '50%',
              background: 'linear-gradient(135deg, #FFE5CC 0%, #FFD4A8 100%)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '20px',
              boxShadow: '0 2px 8px rgba(255, 180, 100, 0.3)'
            }}>
              🛒
            </div>
          </div>
        </header>

        {/* Suggestions Section */}
        {suggestions.length > 0 && (
          <section style={{ marginBottom: '28px' }}>
            <div style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              marginBottom: '12px'
            }}>
              <div style={{
                width: '6px',
                height: '6px',
                borderRadius: '50%',
                background: '#F59E0B',
                animation: 'pulse 2s infinite'
              }} />
              <span style={{
                fontSize: '13px',
                fontWeight: '500',
                color: '#B8A080',
                textTransform: 'uppercase',
                letterSpacing: '0.5px'
              }}>
                Suggested
              </span>
            </div>
            <div style={{
              display: 'flex',
              gap: '10px',
              overflowX: 'auto',
              paddingBottom: '8px',
              marginLeft: '-20px',
              marginRight: '-20px',
              paddingLeft: '20px',
              paddingRight: '20px'
            }}>
              {suggestions.map((suggestion, index) => (
                <div
                  key={suggestion.id}
                  style={{
                    flexShrink: 0,
                    background: 'white',
                    borderRadius: '16px',
                    padding: '14px 16px',
                    boxShadow: '0 2px 12px rgba(0, 0, 0, 0.04)',
                    border: '1px solid rgba(0, 0, 0, 0.04)',
                    minWidth: '140px',
                    cursor: 'pointer',
                    transition: 'all 0.2s ease',
                    animation: `slideIn 0.4s ease ${index * 0.1}s both`
                  }}
                  onClick={() => addSuggestion(suggestion)}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.transform = 'translateY(-2px)';
                    e.currentTarget.style.boxShadow = '0 4px 16px rgba(0, 0, 0, 0.08)';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.transform = 'translateY(0)';
                    e.currentTarget.style.boxShadow = '0 2px 12px rgba(0, 0, 0, 0.04)';
                  }}
                >
                  <div style={{
                    display: 'flex',
                    alignItems: 'flex-start',
                    justifyContent: 'space-between',
                    marginBottom: '8px'
                  }}>
                    <span style={{
                      fontSize: '15px',
                      fontWeight: '500',
                      color: '#1F1F1F'
                    }}>
                      {suggestion.name}
                    </span>
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        dismissSuggestion(suggestion.id);
                      }}
                      style={{
                        background: 'none',
                        border: 'none',
                        padding: '0',
                        cursor: 'pointer',
                        color: '#CCC',
                        fontSize: '16px',
                        lineHeight: 1,
                        marginTop: '-2px'
                      }}
                    >
                      ×
                    </button>
                  </div>
                  <div style={{
                    fontSize: '12px',
                    color: '#B8A080'
                  }}>
                    {suggestion.reason}
                  </div>
                  {suggestion.meal && (
                    <div style={{
                      marginTop: '8px',
                      display: 'inline-flex',
                      alignItems: 'center',
                      gap: '4px',
                      padding: '3px 8px',
                      borderRadius: '20px',
                      background: `${mealColors[suggestion.meal]}15`,
                      fontSize: '11px',
                      color: mealColors[suggestion.meal]
                    }}>
                      {mealEmojis[suggestion.meal]} {suggestion.meal}
                    </div>
                  )}
                </div>
              ))}
            </div>
          </section>
        )}

        {/* Add Item Input */}
        <div style={{
          background: 'white',
          borderRadius: '16px',
          padding: '4px',
          boxShadow: '0 2px 12px rgba(0, 0, 0, 0.04)',
          border: '1px solid rgba(0, 0, 0, 0.04)',
          marginBottom: '24px',
          display: 'flex',
          alignItems: 'center'
        }}>
          <input
            type="text"
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && addItem()}
            placeholder="Add an item..."
            style={{
              flex: 1,
              border: 'none',
              outline: 'none',
              padding: '14px 16px',
              fontSize: '15px',
              background: 'transparent',
              color: '#1F1F1F'
            }}
          />
          <button
            onClick={addItem}
            style={{
              background: inputValue ? 'linear-gradient(135deg, #1F1F1F 0%, #3D3D3D 100%)' : '#F0F0F0',
              border: 'none',
              borderRadius: '12px',
              width: '44px',
              height: '44px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              cursor: inputValue ? 'pointer' : 'default',
              transition: 'all 0.2s ease',
              color: inputValue ? 'white' : '#AAA'
            }}
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
              <line x1="12" y1="5" x2="12" y2="19" />
              <line x1="5" y1="12" x2="19" y2="12" />
            </svg>
          </button>
        </div>

        {/* Shopping List */}
        <section>
          <div style={{
            display: 'flex',
            flexDirection: 'column',
            gap: '8px'
          }}>
            {uncheckedItems.map((item, index) => (
              <div
                key={item.id}
                style={{
                  background: 'white',
                  borderRadius: '14px',
                  padding: '14px 16px',
                  boxShadow: '0 1px 8px rgba(0, 0, 0, 0.03)',
                  border: '1px solid rgba(0, 0, 0, 0.03)',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '14px',
                  cursor: 'pointer',
                  transition: 'all 0.2s ease',
                  opacity: animatingItems.has(item.id) ? 0.5 : 1,
                  transform: animatingItems.has(item.id) ? 'scale(0.98)' : 'scale(1)',
                  animation: item.isNew ? 'slideDown 0.3s ease' : `fadeIn 0.3s ease ${index * 0.05}s both`
                }}
                onClick={() => toggleItem(item.id)}
              >
                <div style={{
                  width: '24px',
                  height: '24px',
                  borderRadius: '8px',
                  border: '2px solid #E0E0E0',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  transition: 'all 0.2s ease',
                  flexShrink: 0
                }}>
                </div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{
                    fontSize: '15px',
                    fontWeight: '500',
                    color: '#1F1F1F',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '8px'
                  }}>
                    {item.name}
                    {item.suggested && (
                      <span style={{
                        fontSize: '10px',
                        padding: '2px 6px',
                        borderRadius: '4px',
                        background: '#FFF3E0',
                        color: '#F59E0B',
                        fontWeight: '600'
                      }}>
                        SMART
                      </span>
                    )}
                  </div>
                  {item.cycle && (
                    <div style={{
                      fontSize: '12px',
                      color: '#AAA',
                      marginTop: '2px'
                    }}>
                      Every ~{item.cycle} days
                    </div>
                  )}
                </div>
                {item.meal ? (
                  <div
                    onClick={(e) => {
                      e.stopPropagation();
                      setShowMealTag(showMealTag === item.id ? null : item.id);
                    }}
                    style={{
                      padding: '4px 10px',
                      borderRadius: '20px',
                      background: `${mealColors[item.meal]}15`,
                      fontSize: '12px',
                      color: mealColors[item.meal],
                      display: 'flex',
                      alignItems: 'center',
                      gap: '4px'
                    }}
                  >
                    {mealEmojis[item.meal]}
                  </div>
                ) : (
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      setShowMealTag(showMealTag === item.id ? null : item.id);
                    }}
                    style={{
                      background: 'none',
                      border: '1px dashed #DDD',
                      borderRadius: '20px',
                      padding: '4px 10px',
                      fontSize: '12px',
                      color: '#BBB',
                      cursor: 'pointer'
                    }}
                  >
                    + tag
                  </button>
                )}
                
                {/* Meal tag dropdown */}
                {showMealTag === item.id && (
                  <div style={{
                    position: 'absolute',
                    right: '36px',
                    marginTop: '80px',
                    background: 'white',
                    borderRadius: '12px',
                    padding: '8px',
                    boxShadow: '0 4px 20px rgba(0, 0, 0, 0.15)',
                    display: 'flex',
                    gap: '6px',
                    zIndex: 10
                  }}>
                    {meals.map(meal => (
                      <button
                        key={meal}
                        onClick={(e) => {
                          e.stopPropagation();
                          setMealTag(item.id, meal);
                        }}
                        style={{
                          background: item.meal === meal ? `${mealColors[meal]}20` : 'transparent',
                          border: 'none',
                          borderRadius: '8px',
                          padding: '8px',
                          fontSize: '18px',
                          cursor: 'pointer',
                          transition: 'all 0.15s ease'
                        }}
                      >
                        {mealEmojis[meal]}
                      </button>
                    ))}
                  </div>
                )}
              </div>
            ))}
          </div>

          {/* Checked Items */}
          {checkedItems.length > 0 && (
            <div style={{ marginTop: '32px' }}>
              <div style={{
                fontSize: '13px',
                fontWeight: '500',
                color: '#CCC',
                textTransform: 'uppercase',
                letterSpacing: '0.5px',
                marginBottom: '12px'
              }}>
                In Cart ({checkedItems.length})
              </div>
              <div style={{
                display: 'flex',
                flexDirection: 'column',
                gap: '6px'
              }}>
                {checkedItems.map(item => (
                  <div
                    key={item.id}
                    style={{
                      background: 'rgba(255, 255, 255, 0.5)',
                      borderRadius: '12px',
                      padding: '12px 16px',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '14px',
                      cursor: 'pointer',
                      opacity: animatingItems.has(item.id) ? 0.5 : 0.6
                    }}
                    onClick={() => toggleItem(item.id)}
                  >
                    <div style={{
                      width: '24px',
                      height: '24px',
                      borderRadius: '8px',
                      background: 'linear-gradient(135deg, #10B981 0%, #34D399 100%)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      flexShrink: 0
                    }}>
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
                        <polyline points="20 6 9 17 4 12" />
                      </svg>
                    </div>
                    <span style={{
                      fontSize: '15px',
                      color: '#999',
                      textDecoration: 'line-through'
                    }}>
                      {item.name}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </section>

        {/* Bottom Info Card */}
        <div style={{
          marginTop: '40px',
          background: 'linear-gradient(135deg, #1F1F1F 0%, #2D2D2D 100%)',
          borderRadius: '20px',
          padding: '24px',
          color: 'white'
        }}>
          <div style={{
            fontSize: '13px',
            fontWeight: '500',
            color: 'rgba(255,255,255,0.5)',
            textTransform: 'uppercase',
            letterSpacing: '0.5px',
            marginBottom: '8px'
          }}>
            ✨ Learning Mode Active
          </div>
          <div style={{
            fontSize: '15px',
            lineHeight: '1.5',
            color: 'rgba(255,255,255,0.8)'
          }}>
            Restock is learning your patterns. The more you use it, the smarter suggestions become.
          </div>
          <div style={{
            marginTop: '16px',
            display: 'flex',
            gap: '16px'
          }}>
            <div>
              <div style={{ fontSize: '24px', fontWeight: '600' }}>12</div>
              <div style={{ fontSize: '12px', color: 'rgba(255,255,255,0.5)' }}>Items tracked</div>
            </div>
            <div>
              <div style={{ fontSize: '24px', fontWeight: '600' }}>3</div>
              <div style={{ fontSize: '12px', color: 'rgba(255,255,255,0.5)' }}>Patterns found</div>
            </div>
          </div>
        </div>
      </div>

      {/* Bottom Navigation */}
      <nav style={{
        position: 'fixed',
        bottom: 0,
        left: 0,
        right: 0,
        background: 'rgba(255, 255, 255, 0.9)',
        backdropFilter: 'blur(20px)',
        borderTop: '1px solid rgba(0, 0, 0, 0.05)',
        padding: '12px 0 28px 0'
      }}>
        <div style={{
          maxWidth: '420px',
          margin: '0 auto',
          display: 'flex',
          justifyContent: 'space-around'
        }}>
          {[
            { id: 'list', icon: '📝', label: 'List' },
            { id: 'meals', icon: '🍽️', label: 'Meals' },
            { id: 'history', icon: '📊', label: 'History' }
          ].map(tab => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              style={{
                background: 'none',
                border: 'none',
                padding: '8px 24px',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                gap: '4px',
                cursor: 'pointer',
                opacity: activeTab === tab.id ? 1 : 0.4,
                transition: 'all 0.2s ease'
              }}
            >
              <span style={{ fontSize: '20px' }}>{tab.icon}</span>
              <span style={{
                fontSize: '11px',
                fontWeight: '500',
                color: '#1F1F1F'
              }}>
                {tab.label}
              </span>
            </button>
          ))}
        </div>
      </nav>

      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&display=swap');
        
        @keyframes pulse {
          0%, 100% { opacity: 1; }
          50% { opacity: 0.4; }
        }
        
        @keyframes slideIn {
          from {
            opacity: 0;
            transform: translateX(20px);
          }
          to {
            opacity: 1;
            transform: translateX(0);
          }
        }
        
        @keyframes fadeIn {
          from {
            opacity: 0;
            transform: translateY(10px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }
        
        @keyframes slideDown {
          from {
            opacity: 0;
            transform: translateY(-10px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }
        
        * {
          box-sizing: border-box;
        }
        
        input::placeholder {
          color: #CCC;
        }
        
        ::-webkit-scrollbar {
          display: none;
        }
      `}</style>
    </div>
  );
};

export default RestockApp;
